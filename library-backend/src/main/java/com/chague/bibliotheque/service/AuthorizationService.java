package com.chague.bibliotheque.service;

import com.chague.bibliotheque.api.exception.emprunt.EmpruntIsNotExistsException;
import com.chague.bibliotheque.api.exception.user.UtilisateurIsNotExistsException;
import com.chague.bibliotheque.domain.Emprunt;
import com.chague.bibliotheque.domain.Role;
import com.chague.bibliotheque.domain.Utilisateur;
import com.chague.bibliotheque.infrastructure.persistence.EmpruntRepository;
import com.chague.bibliotheque.infrastructure.persistence.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UtilisateurRepository utilisateurRepository;
    private final EmpruntRepository empruntRepository;

    public Utilisateur requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }

        return utilisateurRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Utilisateur introuvable"));
    }

    public boolean isAdmin(Utilisateur user) {
        return user != null && user.getRole() == Role.ADMINISTRATEUR;
    }

    public boolean isBibliothecaire(Utilisateur user) {
        return user != null && user.getRole() == Role.BIBLIOTHECAIRE;
    }

    public boolean isStaff(Utilisateur user) {
        return isAdmin(user) || isBibliothecaire(user);
    }

    public void checkAdmin() {
        Utilisateur currentUser = requireCurrentUser();
        if (!isAdmin(currentUser)) {
            throw new AccessDeniedException("Accès réservé à l'administrateur");
        }
    }

    public void checkManager() {
        Utilisateur currentUser = requireCurrentUser();
        if (!isStaff(currentUser)) {
            throw new AccessDeniedException("Accès réservé au personnel");
        }
    }

    public void checkCanAccessUtilisateur(Long utilisateurId) {
        Utilisateur currentUser = requireCurrentUser();

        if (isStaff(currentUser)) {
            return;
        }

        if (!currentUser.getId().equals(utilisateurId)) {
            throw new AccessDeniedException("Accès refusé à cet utilisateur");
        }
    }

    public void checkCanManageUtilisateur(Utilisateur cible) {
        if (cible == null || cible.getId() == null) {
            throw new UtilisateurIsNotExistsException(null);
        }

        Utilisateur currentUser = requireCurrentUser();

        if (isAdmin(currentUser)) {
            return;
        }

        if (isBibliothecaire(currentUser)) {
            if (cible.getRole() != Role.ADHERENT) {
                throw new AccessDeniedException("Un bibliothécaire ne peut gérer que les adhérents");
            }
            return;
        }

        throw new AccessDeniedException("Accès refusé");
    }

    public void checkCanCreateUtilisateur(Role targetRole) {
        Utilisateur currentUser = requireCurrentUser();

        if (isAdmin(currentUser)) {
            return;
        }

        if (isBibliothecaire(currentUser)) {
            if (targetRole != Role.ADHERENT) {
                throw new AccessDeniedException("Un bibliothécaire ne peut créer que des adhérents");
            }
            return;
        }

        throw new AccessDeniedException("Accès refusé");
    }

    public void checkCanAssignRole(Utilisateur cible, Role nouveauRole) {
        Utilisateur currentUser = requireCurrentUser();

        if (isAdmin(currentUser)) {
            return;
        }

        if (isBibliothecaire(currentUser)) {
            if (cible.getRole() != Role.ADHERENT) {
                throw new AccessDeniedException("Un bibliothécaire ne peut modifier que des adhérents");
            }

            if (nouveauRole != Role.ADHERENT) {
                throw new AccessDeniedException("Un bibliothécaire ne peut pas changer les rôles");
            }

            return;
        }

        throw new AccessDeniedException("Accès refusé");
    }

    public void checkCanAccessEmprunt(Long empruntId) {
        Utilisateur currentUser = requireCurrentUser();

        if (isStaff(currentUser)) {
            return;
        }

        Emprunt emprunt = empruntRepository.findById(empruntId)
                .orElseThrow(() -> new EmpruntIsNotExistsException(empruntId));

        if (emprunt.getUtilisateur() == null || !currentUser.getId().equals(emprunt.getUtilisateur().getId())) {
            throw new AccessDeniedException("Accès refusé à cet emprunt");
        }
    }

    public void checkCanManageEmprunt(Emprunt emprunt) {
        Utilisateur currentUser = requireCurrentUser();

        if (isStaff(currentUser)) {
            return;
        }

        if (!currentUser.getId().equals(emprunt.getUtilisateur().getId())) {
            throw new AccessDeniedException("Accès refusé");
        }
    }
}
