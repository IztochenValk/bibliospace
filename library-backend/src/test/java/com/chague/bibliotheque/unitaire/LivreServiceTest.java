package com.chague.bibliotheque.service;

import com.chague.bibliotheque.api.dto.LivreDto;
import com.chague.bibliotheque.api.exception.livre.DeleteLivreImpossibleException;
import com.chague.bibliotheque.api.exception.livre.LivreIsNotExistsException;
import com.chague.bibliotheque.api.exception.livre.LivreIsPresentException;
import com.chague.bibliotheque.domain.Langue;
import com.chague.bibliotheque.domain.Livre;
import com.chague.bibliotheque.domain.StatutEmprunt;
import com.chague.bibliotheque.infrastructure.persistence.CategorieRepository;
import com.chague.bibliotheque.infrastructure.persistence.EmpruntRepository;
import com.chague.bibliotheque.infrastructure.persistence.LivreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de {@link LivreService}.
 *
 * Couvrent les regles metier essentielles : unicite (titre + auteur),
 * calcul de disponibilite, garde-fou de mise a jour de quantite face aux
 * emprunts en cours, et suppression conditionnelle.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LivreService — logique métier du livre")
class LivreServiceTest {

    @Mock
    private LivreRepository livreRepository;

    @Mock
    private CategorieRepository categorieRepository;

    @Mock
    private EmpruntRepository empruntRepository;

    @Mock
    private LivreImageStorageService livreImageStorageService;

    @InjectMocks
    private LivreService livreService;

    private Livre livre;

    @BeforeEach
    void setUp() {
        livre = new Livre();
        livre.setId(1L);
        livre.setTitre("Le Petit Prince");
        livre.setAuteur("Saint-Exupéry");
        livre.setLangue(Langue.FR);
        livre.setQuantiteTotale(5);

        // Le storage service ne fait que normaliser le filename, ce qui n'est pas utile pour nos tests.
        // Du coup, on retourne la valeur d'entree pour ne pas perturber les assertions (et éviter ainsi UnnecessaryStubbingException)
        // lenient() car la plupart des tests (calculateDisponible, update_missing,
        // delete_*, etc.) ne passent jamais par le code qui appelle ce service.
        lenient().when(livreImageStorageService.normalizeStoredFilename(any()))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("crée un livre quand titre + auteur sont uniques")
        void create_ok() {
            LivreDto.CreateLivreRequest req = new LivreDto.CreateLivreRequest(
                    "L'Étranger", "Albert Camus", null, Langue.FR,
                    null, null, 3, List.of()
            );

            when(livreRepository.existsByTitreIgnoreCaseAndAuteurIgnoreCase(
                    "L'Étranger", "Albert Camus")).thenReturn(false);
            when(livreRepository.save(any(Livre.class))).thenAnswer(inv -> {
                Livre l = inv.getArgument(0);
                l.setId(42L);
                return l;
            });

            LivreDto.LivreResponse response = livreService.create(req);

            assertThat(response.id()).isEqualTo(42L);
            assertThat(response.titre()).isEqualTo("L'Étranger");
            assertThat(response.quantiteTotale()).isEqualTo(3);
            // Aucun emprunt en cours -> disponibilite = quantite totale
            assertThat(response.quantiteDisponible()).isEqualTo(3);
        }

        @Test
        @DisplayName("rejette un doublon titre + auteur (insensible à la casse)")
        void create_duplicate() {
            LivreDto.CreateLivreRequest req = new LivreDto.CreateLivreRequest(
                    "Le Petit Prince", "Saint-Exupéry", null, Langue.FR,
                    null, null, 1, null
            );

            when(livreRepository.existsByTitreIgnoreCaseAndAuteurIgnoreCase(
                    "Le Petit Prince", "Saint-Exupéry")).thenReturn(true);

            assertThatThrownBy(() -> livreService.create(req))
                    .isInstanceOf(LivreIsPresentException.class);

            verify(livreRepository, never()).save(any());
        }

        @Test
        @DisplayName("normalise quantiteTotale null en 0")
        void create_quantite_null() {
            LivreDto.CreateLivreRequest req = new LivreDto.CreateLivreRequest(
                    "Test", "Auteur", null, Langue.EN,
                    null, null, null, null
            );

            when(livreRepository.existsByTitreIgnoreCaseAndAuteurIgnoreCase(
                    "Test", "Auteur")).thenReturn(false);
            when(livreRepository.save(any(Livre.class))).thenAnswer(inv -> inv.getArgument(0));

            ArgumentCaptor<Livre> captor = ArgumentCaptor.forClass(Livre.class);
            livreService.create(req);
            verify(livreRepository).save(captor.capture());

            assertThat(captor.getValue().getQuantiteTotale()).isZero();
        }
    }

    @Nested
    @DisplayName("calculateDisponible()")
    class CalculateDisponible {

        @Test
        @DisplayName("retourne quantité totale - emprunts en cours")
        void disponible_normal() {
            when(empruntRepository.countByLivreIdAndStatut(1L, StatutEmprunt.EN_COURS))
                    .thenReturn(2L);

            int dispo = livreService.calculateDisponible(livre);

            assertThat(dispo).isEqualTo(3); // 5 total - 2 en cours
        }

        @Test
        @DisplayName("retourne 0 si tous les exemplaires sont empruntés")
        void disponible_zero() {
            when(empruntRepository.countByLivreIdAndStatut(1L, StatutEmprunt.EN_COURS))
                    .thenReturn(5L);

            int dispo = livreService.calculateDisponible(livre);

            assertThat(dispo).isZero();
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("rejette une nouvelle quantité inférieure au nombre d'emprunts en cours")
        void update_quantite_inferieure_aux_emprunts() {
            // 5 exemplaires, 3 deja empruntes : on essaie de passer la quantite a 2
            LivreDto.UpdateLivreRequest req = new LivreDto.UpdateLivreRequest(
                    "Le Petit Prince", "Saint-Exupéry", null, Langue.FR,
                    null, null, 2, null
            );

            when(livreRepository.findById(1L)).thenReturn(Optional.of(livre));
            when(empruntRepository.countByLivreIdAndStatut(1L, StatutEmprunt.EN_COURS))
                    .thenReturn(3L);

            assertThatThrownBy(() -> livreService.update(1L, req))
                    .isInstanceOf(DeleteLivreImpossibleException.class);

            verify(livreRepository, never()).save(any());
        }

        @Test
        @DisplayName("lève LivreIsNotExistsException si le livre cible n'existe pas")
        void update_missing() {
            LivreDto.UpdateLivreRequest req = new LivreDto.UpdateLivreRequest(
                    "X", "Y", null, Langue.FR, null, null, 1, null
            );

            when(livreRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> livreService.update(404L, req))
                    .isInstanceOf(LivreIsNotExistsException.class);
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("supprime le livre quand aucun emprunt n'existe pour ce livre")
        void delete_ok() {
            when(livreRepository.findById(1L)).thenReturn(Optional.of(livre));
            when(empruntRepository.existsByLivreId(1L)).thenReturn(false);

            livreService.delete(1L);

            verify(livreRepository).delete(livre);
        }

        @Test
        @DisplayName("rejette la suppression si le livre a déjà été emprunté (historique conservé)")
        void delete_with_history() {
            when(livreRepository.findById(1L)).thenReturn(Optional.of(livre));
            when(empruntRepository.existsByLivreId(1L)).thenReturn(true);

            assertThatThrownBy(() -> livreService.delete(1L))
                    .isInstanceOf(DeleteLivreImpossibleException.class);

            verify(livreRepository, never()).delete(any(Livre.class));
        }

        @Test
        @DisplayName("lève LivreIsNotExistsException si le livre n'existe pas")
        void delete_missing() {
            when(livreRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> livreService.delete(404L))
                    .isInstanceOf(LivreIsNotExistsException.class);

            verify(empruntRepository, never()).existsByLivreId(anyLong());
        }
    }
}
