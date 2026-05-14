package com.chague.bibliotheque.service;

import com.chague.bibliotheque.api.exception.user.UtilisateurIsNotExistsException;
import com.chague.bibliotheque.domain.Role;
import com.chague.bibliotheque.domain.StatutUtilisateur;
import com.chague.bibliotheque.domain.Utilisateur;
import com.chague.bibliotheque.infrastructure.persistence.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de {@link UtilisateurService}.
 *
 * Ciblage prioritaire : la methode anonymize() qui implemente la conformite
 * RGPD. La specificite RGPD est de SUBSTITUER les donnees identifiantes par
 * des valeurs marqueurs ("ANONYMISE", "UTILISATEUR-{id}", etc.) plutot que de
 * supprimer la ligne, afin de preserver l'integrite referentielle de
 * l'historique des emprunts.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UtilisateurService — anonymisation RGPD et garde-fous")
class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private UtilisateurService utilisateurService;

    private Utilisateur adherent;

    @BeforeEach
    void setUp() {
        adherent = new Utilisateur();
        adherent.setId(42L);
        adherent.setNom("Durand");
        adherent.setPrenom("Alice");
        adherent.setEmail("alice@example.com");
        adherent.setRole(Role.ADHERENT);
        adherent.setStatut(StatutUtilisateur.ACTIF);
    }

    @Nested
    @DisplayName("anonymize() — conformité RGPD")
    class Anonymize {

        @Test
        @DisplayName("substitue les données personnelles et bascule le statut à ANONYMISE")
        void anonymize_remplace_les_donnees() {
            when(utilisateurRepository.findById(42L)).thenReturn(Optional.of(adherent));
            when(passwordEncoder.encode(any())).thenReturn("hashed-random");
            when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(inv -> inv.getArgument(0));

            utilisateurService.anonymize(42L);

            // Verification : les champs identifiants ont ete substitues par
            // des valeurs marqueurs, pas mis a null. C'est la specificite RGPD :
            // on ne supprime pas la ligne (l'historique des emprunts doit
            // conserver sa cle etrangere), on neutralise les donnees.
            assertThat(adherent.getNom()).isEqualTo("ANONYMISE");
            assertThat(adherent.getPrenom()).isEqualTo("UTILISATEUR-42");
            assertThat(adherent.getEmail()).isEqualTo("anonyme-42@library.local");
            assertThat(adherent.getMotDePasse()).isEqualTo("hashed-random");
            assertThat(adherent.getStatut()).isEqualTo(StatutUtilisateur.ANONYMISE);
            assertThat(adherent.getStatutChangedAt()).isNotNull();

            verify(authService).forceReturnAllCurrentLoansForUser(42L);
            verify(utilisateurRepository).save(adherent);
        }

        @Test
        @DisplayName("rejette l'anonymisation d'un utilisateur staff (réservée aux adhérents)")
        void anonymize_rejette_staff() {
            adherent.setRole(Role.BIBLIOTHECAIRE);
            when(utilisateurRepository.findById(42L)).thenReturn(Optional.of(adherent));

            assertThatThrownBy(() -> utilisateurService.anonymize(42L))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("réservée aux adhérents");

            verify(utilisateurRepository, never()).save(any());
            verify(authService, never()).forceReturnAllCurrentLoansForUser(any());
        }

        @Test
        @DisplayName("est idempotent : anonymiser un utilisateur déjà anonymisé est un no-op silencieux")
        void anonymize_idempotent() {
            adherent.setStatut(StatutUtilisateur.ANONYMISE);
            adherent.setNom("ANONYMISE");
            when(utilisateurRepository.findById(42L)).thenReturn(Optional.of(adherent));

            utilisateurService.anonymize(42L);

            // Ne re-substitue pas, ne re-sauvegarde pas, ne force pas le retour
            // des emprunts (deja fait lors de la premiere anonymisation)
            verify(utilisateurRepository, never()).save(any());
            verify(authService, never()).forceReturnAllCurrentLoansForUser(any());
        }

        @Test
        @DisplayName("lève UtilisateurIsNotExistsException si l'utilisateur cible n'existe pas")
        void anonymize_missing() {
            when(utilisateurRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> utilisateurService.anonymize(404L))
                    .isInstanceOf(UtilisateurIsNotExistsException.class);

            verify(utilisateurRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deactivate()")
    class Deactivate {

        @Test
        @DisplayName("rejette la désactivation d'un compte ADMINISTRATEUR")
        void deactivate_rejette_admin() {
            adherent.setRole(Role.ADMINISTRATEUR);
            when(utilisateurRepository.findById(42L)).thenReturn(Optional.of(adherent));

            assertThatThrownBy(() -> utilisateurService.deactivate(42L))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("désactivation de l'administrateur");

            verify(utilisateurRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("reactivate()")
    class Reactivate {

        @Test
        @DisplayName("rejette la réactivation d'un compte ANONYMISE (irréversible)")
        void reactivate_rejette_anonymise() {
            adherent.setStatut(StatutUtilisateur.ANONYMISE);
            when(utilisateurRepository.findById(42L)).thenReturn(Optional.of(adherent));

            assertThatThrownBy(() -> utilisateurService.reactivate(42L))
                    .hasMessageContaining("anonymisé ne peut pas être réactivé");

            verify(utilisateurRepository, never()).save(any());
        }
    }
}
