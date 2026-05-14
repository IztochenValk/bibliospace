package com.chague.bibliotheque.service;

import com.chague.bibliotheque.api.dto.EmpruntDto;
import com.chague.bibliotheque.api.exception.emprunt.EmpruntImpossibleException;
import com.chague.bibliotheque.api.exception.emprunt.EmpruntIsNotExistsException;
import com.chague.bibliotheque.api.exception.livre.LivreIsNotExistsException;
import com.chague.bibliotheque.domain.Emprunt;
import com.chague.bibliotheque.domain.Livre;
import com.chague.bibliotheque.domain.Role;
import com.chague.bibliotheque.domain.StatutEmprunt;
import com.chague.bibliotheque.domain.Utilisateur;
import com.chague.bibliotheque.infrastructure.persistence.EmpruntRepository;
import com.chague.bibliotheque.infrastructure.persistence.LivreRepository;
import com.chague.bibliotheque.infrastructure.persistence.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires couvrant la logique métier de {@link EmpruntService}.
 * <p>
 * Aligné sur le modèle simplifié post-fusion : Livre porte directement quantiteTotale,
 * il n'y a plus d'entité InventaireLivre intermédiaire ni de StockService dédié.
 * Le calcul de disponibilité est porté par {@link LivreService#calculateDisponible(Livre)}.
 * <p>
 * Règles de gestion couvertes :
 * <ul>
 *     <li>RG-01 / RG-02 : contrôle d'accès selon le rôle de l'utilisateur authentifié.</li>
 *     <li>RG-03 : un emprunt n'est possible que si un exemplaire est disponible.</li>
 *     <li>RG-04 : la disponibilité est calculée dynamiquement.</li>
 *     <li>RG-05 : la date de retour effective ne peut être antérieure à la date d'emprunt
 *                 ni postérieure à la date du jour.</li>
 *     <li>Un même utilisateur ne peut pas avoir deux emprunts EN_COURS sur le même livre.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmpruntService — logique métier de l'emprunt et du retour")
class EmpruntServiceTest {

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private EmpruntRepository empruntRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private LivreRepository livreRepository;

    @Mock
    private LivreService livreService;

    @InjectMocks
    private EmpruntService empruntService;

    // ------------------------------------------------------------------
    // Fixtures
    // ------------------------------------------------------------------

    private Utilisateur adherent;
    private Utilisateur bibliothecaire;
    private Livre livre;

    @BeforeEach
    void setUp() {
        adherent = new Utilisateur();
        adherent.setId(10L);
        adherent.setNom("Durand");
        adherent.setPrenom("Alice");
        adherent.setEmail("alice@example.com");
        adherent.setRole(Role.ADHERENT);

        bibliothecaire = new Utilisateur();
        bibliothecaire.setId(20L);
        bibliothecaire.setNom("Martin");
        bibliothecaire.setPrenom("Paul");
        bibliothecaire.setEmail("paul@example.com");
        bibliothecaire.setRole(Role.BIBLIOTHECAIRE);

        livre = new Livre();
        livre.setId(100L);
        livre.setTitre("L'Étranger");
        livre.setAuteur("Albert Camus");
        livre.setQuantiteTotale(3);
    }

    // ==================================================================
    // create()
    // ==================================================================

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("crée un emprunt EN_COURS pour un adhérent quand un exemplaire est disponible")
        void create_ok() {
            LocalDate dateRetour = LocalDate.now().plusDays(14);
            EmpruntDto.CreateEmpruntRequest req =
                    new EmpruntDto.CreateEmpruntRequest(livre.getId(), dateRetour);

            when(authorizationService.requireCurrentUser()).thenReturn(adherent);
            when(livreRepository.findById(livre.getId())).thenReturn(Optional.of(livre));
            when(empruntRepository.existsByUtilisateurIdAndLivreIdAndStatut(
                    adherent.getId(), livre.getId(), StatutEmprunt.EN_COURS)).thenReturn(false);
            when(livreService.calculateDisponible(livre)).thenReturn(2);
            when(empruntRepository.save(any(Emprunt.class))).thenAnswer(inv -> {
                Emprunt e = inv.getArgument(0);
                e.setId(999L);
                return e;
            });

            EmpruntDto.EmpruntResponse response = empruntService.create(req);

            assertThat(response.id()).isEqualTo(999L);
            assertThat(response.utilisateurId()).isEqualTo(adherent.getId());
            assertThat(response.livreId()).isEqualTo(livre.getId());
            assertThat(response.titreLivre()).isEqualTo("L'Étranger");
            assertThat(response.statut()).isEqualTo(StatutEmprunt.EN_COURS);
            assertThat(response.dateEmprunt()).isEqualTo(LocalDate.now());
            assertThat(response.dateRetourPrevue()).isEqualTo(dateRetour);
            assertThat(response.dateRetourEffective()).isNull();

            ArgumentCaptor<Emprunt> captor = ArgumentCaptor.forClass(Emprunt.class);
            verify(empruntRepository).save(captor.capture());
            Emprunt persisted = captor.getValue();
            assertThat(persisted.getStatut()).isEqualTo(StatutEmprunt.EN_COURS);
            assertThat(persisted.getUtilisateur()).isSameAs(adherent);
            assertThat(persisted.getLivre()).isSameAs(livre);
        }

        // NOTE : le test "refuse si non adherent" a ete retire. Le controle
        // de role est desormais declaratif via @PreAuthorize("hasRole('ADHERENT')")
        // sur la methode EmpruntService.create. Cet aspect est intercepte
        // par Spring AOP au runtime, mais un test unitaire Mockito instancie
        // la classe DIRECTEMENT sans proxy : le check de role ne s'applique
        // pas dans ce contexte de test. La verification du comportement
        // RBAC est deplacee dans les tests d'integration Spring Security.

        @Test
        @DisplayName("lève LivreIsNotExistsException si le livre cible n'existe pas")
        void create_missing_livre() {
            EmpruntDto.CreateEmpruntRequest req =
                    new EmpruntDto.CreateEmpruntRequest(404L, LocalDate.now().plusDays(7));

            when(authorizationService.requireCurrentUser()).thenReturn(adherent);
            when(livreRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> empruntService.create(req))
                    .isInstanceOf(LivreIsNotExistsException.class);

            verify(empruntRepository, never()).save(any());
        }

        @Test
        @DisplayName("refuse si l'adhérent a déjà un emprunt EN_COURS pour ce livre")
        void create_rejects_duplicate_active_loan() {
            EmpruntDto.CreateEmpruntRequest req =
                    new EmpruntDto.CreateEmpruntRequest(livre.getId(), LocalDate.now().plusDays(7));

            when(authorizationService.requireCurrentUser()).thenReturn(adherent);
            when(livreRepository.findById(livre.getId())).thenReturn(Optional.of(livre));
            when(empruntRepository.existsByUtilisateurIdAndLivreIdAndStatut(
                    adherent.getId(), livre.getId(), StatutEmprunt.EN_COURS)).thenReturn(true);

            assertThatThrownBy(() -> empruntService.create(req))
                    .isInstanceOf(EmpruntImpossibleException.class)
                    .hasMessageContaining("déjà emprunté");

            verify(empruntRepository, never()).save(any());
            verify(livreService, never()).calculateDisponible(any());
        }

        @Test
        @DisplayName("refuse si aucun exemplaire n'est disponible (stock <= 0)")
        void create_rejects_when_no_stock() {
            EmpruntDto.CreateEmpruntRequest req =
                    new EmpruntDto.CreateEmpruntRequest(livre.getId(), LocalDate.now().plusDays(7));

            when(authorizationService.requireCurrentUser()).thenReturn(adherent);
            when(livreRepository.findById(livre.getId())).thenReturn(Optional.of(livre));
            when(empruntRepository.existsByUtilisateurIdAndLivreIdAndStatut(
                    adherent.getId(), livre.getId(), StatutEmprunt.EN_COURS)).thenReturn(false);
            when(livreService.calculateDisponible(livre)).thenReturn(0);

            assertThatThrownBy(() -> empruntService.create(req))
                    .isInstanceOf(EmpruntImpossibleException.class)
                    .hasMessageContaining("Aucun exemplaire disponible");

            verify(empruntRepository, never()).save(any());
        }
    }

    // ==================================================================
    // retourner()
    // ==================================================================

    @Nested
    @DisplayName("retourner()")
    class Retourner {

        private Emprunt empruntEnCours(LocalDate dateEmprunt) {
            Emprunt e = new Emprunt();
            e.setId(7L);
            e.setUtilisateur(adherent);
            e.setLivre(livre);
            e.setDateEmprunt(dateEmprunt);
            e.setDateRetourPrevue(dateEmprunt.plusDays(14));
            e.setStatut(StatutEmprunt.EN_COURS);
            return e;
        }

        @Test
        @DisplayName("retourne un emprunt EN_COURS et bascule son statut à RETOURNE")
        void retourner_ok_avec_date_fournie() {
            Emprunt emprunt = empruntEnCours(LocalDate.now().minusDays(3));
            LocalDate dateRetour = LocalDate.now().minusDays(1);
            EmpruntDto.RetourEmpruntRequest req = new EmpruntDto.RetourEmpruntRequest(dateRetour);

            when(empruntRepository.findById(7L)).thenReturn(Optional.of(emprunt));
            when(empruntRepository.save(any(Emprunt.class))).thenAnswer(inv -> inv.getArgument(0));

            EmpruntDto.EmpruntResponse response = empruntService.retourner(7L, req);

            assertThat(response.statut()).isEqualTo(StatutEmprunt.RETOURNE);
            assertThat(response.dateRetourEffective()).isEqualTo(dateRetour);
            verify(authorizationService).checkCanManageEmprunt(emprunt);
        }

        @Test
        @DisplayName("utilise la date du jour quand aucune date de retour n'est fournie")
        void retourner_ok_sans_date_fournie() {
            Emprunt emprunt = empruntEnCours(LocalDate.now().minusDays(3));
            EmpruntDto.RetourEmpruntRequest req = new EmpruntDto.RetourEmpruntRequest(null);

            when(empruntRepository.findById(7L)).thenReturn(Optional.of(emprunt));
            when(empruntRepository.save(any(Emprunt.class))).thenAnswer(inv -> inv.getArgument(0));

            EmpruntDto.EmpruntResponse response = empruntService.retourner(7L, req);

            assertThat(response.dateRetourEffective()).isEqualTo(LocalDate.now());
            assertThat(response.statut()).isEqualTo(StatutEmprunt.RETOURNE);
        }

        @Test
        @DisplayName("lève EmpruntIsNotExistsException si l'emprunt n'existe pas")
        void retourner_missing() {
            when(empruntRepository.findById(404L)).thenReturn(Optional.empty());
            EmpruntDto.RetourEmpruntRequest req = new EmpruntDto.RetourEmpruntRequest(null);

            assertThatThrownBy(() -> empruntService.retourner(404L, req))
                    .isInstanceOf(EmpruntIsNotExistsException.class);

            verify(authorizationService, never()).checkCanManageEmprunt(any());
            verify(empruntRepository, never()).save(any());
        }

        @Test
        @DisplayName("refuse un double retour (emprunt déjà RETOURNE)")
        void retourner_deja_retourne() {
            Emprunt emprunt = empruntEnCours(LocalDate.now().minusDays(3));
            emprunt.setStatut(StatutEmprunt.RETOURNE);
            EmpruntDto.RetourEmpruntRequest req = new EmpruntDto.RetourEmpruntRequest(null);

            when(empruntRepository.findById(7L)).thenReturn(Optional.of(emprunt));

            assertThatThrownBy(() -> empruntService.retourner(7L, req))
                    .isInstanceOf(EmpruntImpossibleException.class)
                    .hasMessageContaining("n'est pas en cours");

            verify(empruntRepository, never()).save(any());
        }

        @Test
        @DisplayName("refuse une date de retour antérieure à la date d'emprunt")
        void retourner_date_avant_emprunt() {
            LocalDate dateEmprunt = LocalDate.now().minusDays(5);
            Emprunt emprunt = empruntEnCours(dateEmprunt);
            EmpruntDto.RetourEmpruntRequest req =
                    new EmpruntDto.RetourEmpruntRequest(dateEmprunt.minusDays(1));

            when(empruntRepository.findById(7L)).thenReturn(Optional.of(emprunt));

            assertThatThrownBy(() -> empruntService.retourner(7L, req))
                    .isInstanceOf(EmpruntImpossibleException.class)
                    .hasMessageContaining("antérieure à la date d'emprunt");

            verify(empruntRepository, never()).save(any());
        }

        @Test
        @DisplayName("refuse une date de retour dans le futur")
        void retourner_date_future() {
            Emprunt emprunt = empruntEnCours(LocalDate.now().minusDays(3));
            EmpruntDto.RetourEmpruntRequest req =
                    new EmpruntDto.RetourEmpruntRequest(LocalDate.now().plusDays(1));

            when(empruntRepository.findById(7L)).thenReturn(Optional.of(emprunt));

            assertThatThrownBy(() -> empruntService.retourner(7L, req))
                    .isInstanceOf(EmpruntImpossibleException.class)
                    .hasMessageContaining("dans le futur");

            verify(empruntRepository, never()).save(any());
        }
    }

    // ==================================================================
    // getById()
    // ==================================================================

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("retourne la réponse DTO quand l'emprunt existe et l'utilisateur y a accès")
        void getById_ok() {
            Emprunt emprunt = new Emprunt();
            emprunt.setId(42L);
            emprunt.setUtilisateur(adherent);
            emprunt.setLivre(livre);
            emprunt.setDateEmprunt(LocalDate.now().minusDays(1));
            emprunt.setDateRetourPrevue(LocalDate.now().plusDays(10));
            emprunt.setStatut(StatutEmprunt.EN_COURS);

            when(empruntRepository.findById(42L)).thenReturn(Optional.of(emprunt));

            EmpruntDto.EmpruntResponse response = empruntService.getById(42L);

            assertThat(response.id()).isEqualTo(42L);
            verify(authorizationService).checkCanAccessEmprunt(42L);
        }

        @Test
        @DisplayName("lève EmpruntIsNotExistsException si l'emprunt est absent")
        void getById_missing() {
            when(empruntRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> empruntService.getById(404L))
                    .isInstanceOf(EmpruntIsNotExistsException.class);
        }
    }

    // ==================================================================
    // list()
    // ==================================================================

    @Nested
    @DisplayName("list()")
    class ListAll {

        @Test
        @DisplayName("le personnel (bibliothécaire/admin) voit tous les emprunts")
        void list_staff_sees_all() {
            Emprunt e1 = new Emprunt();
            e1.setId(1L);
            e1.setUtilisateur(adherent);
            e1.setLivre(livre);
            e1.setDateEmprunt(LocalDate.now());
            e1.setDateRetourPrevue(LocalDate.now().plusDays(14));
            e1.setStatut(StatutEmprunt.EN_COURS);

            when(authorizationService.requireCurrentUser()).thenReturn(bibliothecaire);
            when(authorizationService.isStaff(bibliothecaire)).thenReturn(true);
            when(empruntRepository.findAll()).thenReturn(List.of(e1));

            List<EmpruntDto.EmpruntResponse> responses = empruntService.list();

            assertThat(responses).hasSize(1);
            verify(empruntRepository).findAll();
            verify(empruntRepository, never()).findByUtilisateurId(anyLong());
        }

        @Test
        @DisplayName("un adhérent ne voit que ses propres emprunts")
        void list_adherent_sees_own() {
            when(authorizationService.requireCurrentUser()).thenReturn(adherent);
            when(authorizationService.isStaff(adherent)).thenReturn(false);
            when(empruntRepository.findByUtilisateurId(adherent.getId())).thenReturn(List.of());

            List<EmpruntDto.EmpruntResponse> responses = empruntService.list();

            assertThat(responses).isEmpty();
            verify(empruntRepository).findByUtilisateurId(adherent.getId());
            verify(empruntRepository, never()).findAll();
        }
    }

    // ==================================================================
    // listByUtilisateur()
    // ==================================================================

    @Nested
    @DisplayName("listByUtilisateur()")
    class ListByUtilisateur {

        @Test
        @DisplayName("appelle le contrôle d'accès puis retourne les emprunts de l'utilisateur")
        void list_by_utilisateur_ok() {
            when(empruntRepository.findByUtilisateurId(adherent.getId())).thenReturn(List.of());

            List<EmpruntDto.EmpruntResponse> responses =
                    empruntService.listByUtilisateur(adherent.getId());

            assertThat(responses).isEmpty();
            verify(authorizationService).checkCanAccessUtilisateur(adherent.getId());
        }

        @Test
        @DisplayName("propage l'AccessDeniedException quand le contrôle d'accès le refuse")
        void list_by_utilisateur_denied() {
            org.mockito.Mockito.doThrow(new AccessDeniedException("nope"))
                    .when(authorizationService).checkCanAccessUtilisateur(adherent.getId());

            assertThatThrownBy(() -> empruntService.listByUtilisateur(adherent.getId()))
                    .isInstanceOf(AccessDeniedException.class);

            verify(empruntRepository, never()).findByUtilisateurId(anyLong());
        }
    }

    // ==================================================================
    // listByStatut()
    // ==================================================================

    @Nested
    @DisplayName("listByStatut()")
    class ListByStatut {

        @Test
        @DisplayName("le personnel voit tous les emprunts du statut demandé")
        void list_by_statut_staff() {
            when(authorizationService.requireCurrentUser()).thenReturn(bibliothecaire);
            when(authorizationService.isStaff(bibliothecaire)).thenReturn(true);
            when(empruntRepository.findByStatut(StatutEmprunt.EN_COURS)).thenReturn(List.of());

            List<EmpruntDto.EmpruntResponse> responses = empruntService.listByStatut("EN_COURS");

            assertThat(responses).isEmpty();
            verify(empruntRepository).findByStatut(StatutEmprunt.EN_COURS);
        }

        @Test
        @DisplayName("l'adhérent ne voit que ses emprunts filtrés par statut")
        void list_by_statut_adherent() {
            Emprunt mien = new Emprunt();
            mien.setId(1L);
            mien.setUtilisateur(adherent);
            mien.setLivre(livre);
            mien.setDateEmprunt(LocalDate.now().minusDays(5));
            mien.setDateRetourPrevue(LocalDate.now().plusDays(9));
            mien.setStatut(StatutEmprunt.EN_COURS);

            Emprunt autre = new Emprunt();
            autre.setId(2L);
            autre.setUtilisateur(adherent);
            autre.setLivre(livre);
            autre.setDateEmprunt(LocalDate.now().minusDays(20));
            autre.setDateRetourPrevue(LocalDate.now().minusDays(6));
            autre.setDateRetourEffective(LocalDate.now().minusDays(6));
            autre.setStatut(StatutEmprunt.RETOURNE);

            when(authorizationService.requireCurrentUser()).thenReturn(adherent);
            when(authorizationService.isStaff(adherent)).thenReturn(false);
            when(empruntRepository.findByUtilisateurId(adherent.getId()))
                    .thenReturn(List.of(mien, autre));

            List<EmpruntDto.EmpruntResponse> responses = empruntService.listByStatut("EN_COURS");

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).statut()).isEqualTo(StatutEmprunt.EN_COURS);
        }

        @Test
        @DisplayName("rejette un statut inconnu avec EmpruntImpossibleException")
        void list_by_statut_invalide() {
            when(authorizationService.requireCurrentUser()).thenReturn(adherent);

            assertThatThrownBy(() -> empruntService.listByStatut("BLABLA"))
                    .isInstanceOf(EmpruntImpossibleException.class)
                    .hasMessageContaining("Statut invalide");
        }

        @Test
        @DisplayName("accepte la normalisation (casse, espaces) du statut")
        void list_by_statut_normalisation() {
            when(authorizationService.requireCurrentUser()).thenReturn(bibliothecaire);
            when(authorizationService.isStaff(bibliothecaire)).thenReturn(true);
            when(empruntRepository.findByStatut(StatutEmprunt.RETOURNE)).thenReturn(List.of());

            empruntService.listByStatut("  retourne  ");

            verify(empruntRepository, times(1)).findByStatut(StatutEmprunt.RETOURNE);
        }
    }
}
