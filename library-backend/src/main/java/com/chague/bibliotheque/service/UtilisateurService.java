package com.chague.bibliotheque.service;

import com.chague.bibliotheque.api.dto.MonCompteDto;
import com.chague.bibliotheque.api.dto.UtilisateurDto;
import com.chague.bibliotheque.api.exception.auth.InvalidCredentialsException;
import com.chague.bibliotheque.api.exception.user.DeleteUtilisateurImpossibleException;
import com.chague.bibliotheque.api.exception.user.UtilisateurIsNotExistsException;
import com.chague.bibliotheque.api.exception.user.UtilisateurIsPresentException;
import com.chague.bibliotheque.domain.Role;
import com.chague.bibliotheque.domain.StatutUtilisateur;
import com.chague.bibliotheque.domain.Utilisateur;
import com.chague.bibliotheque.infrastructure.persistence.UtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@AllArgsConstructor
@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorizationService authorizationService;
    private final AuthService authService;

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','BIBLIOTHECAIRE')")
    @Transactional
    public UtilisateurDto.UtilisateurResponse update(Long id, UtilisateurDto.UpdateUtilisateurRequest req) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurIsNotExistsException(id));

        authorizationService.checkCanManageUtilisateur(utilisateur);

        if (utilisateur.getStatut() != StatutUtilisateur.ACTIF) {
            throw new AccessDeniedException("Impossible de modifier un utilisateur non actif");
        }

        authorizationService.checkCanAssignRole(utilisateur, req.role());

        if (req.role() == Role.ADMINISTRATEUR && utilisateur.getRole() != Role.ADMINISTRATEUR) {
            throw new AccessDeniedException("La promotion vers administrateur n'est pas autorisée");
        }

        String normalizedEmail = normalizeEmail(req.email());

        if (utilisateurRepository.existsByEmailAndIdNot(normalizedEmail, id)) {
            throw new UtilisateurIsPresentException(normalizedEmail);
        }

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        Utilisateur currentUser = currentUserEmail != null
                ? utilisateurRepository.findByEmail(currentUserEmail).orElse(null)
                : null;

        if (currentUser != null
                && currentUser.getId().equals(utilisateur.getId())
                && currentUser.getRole() == Role.ADMINISTRATEUR
                && req.role() != Role.ADMINISTRATEUR) {
            throw new AccessDeniedException("Un administrateur ne peut pas se rétrograder lui-même");
        }

        boolean roleChanged = utilisateur.getRole() != req.role();
        boolean becomesStaff = req.role() == Role.BIBLIOTHECAIRE || req.role() == Role.ADMINISTRATEUR;
        boolean wasAdherent = utilisateur.getRole() == Role.ADHERENT;

        utilisateur.setNom(req.nom().trim());
        utilisateur.setPrenom(req.prenom().trim());
        utilisateur.setEmail(normalizedEmail);
        utilisateur.setRole(req.role());

        Utilisateur saved = utilisateurRepository.save(utilisateur);

        if (roleChanged && wasAdherent && becomesStaff) {
            authService.forceReturnAllCurrentLoansForUser(saved.getId());

            final Long savedId = saved.getId();
            saved = utilisateurRepository.findById(savedId)
                    .orElseThrow(() -> new UtilisateurIsNotExistsException(savedId));
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UtilisateurDto.UtilisateurResponse getById(Long id) {
        authorizationService.checkCanAccessUtilisateur(id);

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurIsNotExistsException(id));

        return toResponse(utilisateur);
    }

    @Transactional(readOnly = true)
    public UtilisateurDto.UtilisateurResponse getMyProfile() {
        Utilisateur currentUser = authorizationService.requireCurrentUser();

        if (currentUser.getStatut() != StatutUtilisateur.ACTIF) {
            throw new AccessDeniedException("Compte non actif");
        }

        return toResponse(currentUser);
    }

    @Transactional
    public UtilisateurDto.UtilisateurResponse updateMyProfile(MonCompteDto.UpdateMyProfileRequest req) {
        Utilisateur currentUser = authorizationService.requireCurrentUser();

        if (currentUser.getStatut() != StatutUtilisateur.ACTIF) {
            throw new AccessDeniedException("Compte non actif");
        }

        String normalizedEmail = normalizeEmail(req.email());

        if (utilisateurRepository.existsByEmailAndIdNot(normalizedEmail, currentUser.getId())) {
            throw new UtilisateurIsPresentException(normalizedEmail);
        }

        currentUser.setNom(req.nom().trim());
        currentUser.setPrenom(req.prenom().trim());
        currentUser.setEmail(normalizedEmail);

        Utilisateur saved = utilisateurRepository.save(currentUser);
        return toResponse(saved);
    }

    @Transactional
    public void updateMyPassword(MonCompteDto.UpdateMyPasswordRequest req) {
        Utilisateur currentUser = authorizationService.requireCurrentUser();

        if (currentUser.getStatut() != StatutUtilisateur.ACTIF) {
            throw new AccessDeniedException("Compte non actif");
        }

        if (!passwordEncoder.matches(req.ancienMotDePasse(), currentUser.getMotDePasse())) {
            throw new InvalidCredentialsException("Ancien mot de passe invalide");
        }

        if (req.ancienMotDePasse().equals(req.nouveauMotDePasse())) {
            throw new InvalidCredentialsException("Le nouveau mot de passe doit être différent de l'ancien");
        }

        currentUser.setMotDePasse(passwordEncoder.encode(req.nouveauMotDePasse()));

        utilisateurRepository.save(currentUser);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','BIBLIOTHECAIRE')")
    @Transactional(readOnly = true)
    public List<UtilisateurDto.UtilisateurResponse> list() {
        return utilisateurRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','BIBLIOTHECAIRE')")
    @Transactional(readOnly = true)
    public List<UtilisateurDto.UtilisateurResponse> listByRole(Role role) {
        return utilisateurRepository.findByRole(role).stream()
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','BIBLIOTHECAIRE')")
    @Transactional(readOnly = true)
    public List<UtilisateurDto.UtilisateurResponse> listByStatut(StatutUtilisateur statut) {
        return utilisateurRepository.findByStatut(statut).stream()
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','BIBLIOTHECAIRE')")
    @Transactional
    public void deactivate(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurIsNotExistsException(id));

        authorizationService.checkCanManageUtilisateur(utilisateur);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        Utilisateur currentUser = currentUserEmail != null
                ? utilisateurRepository.findByEmail(currentUserEmail).orElse(null)
                : null;

        if (currentUser != null && currentUser.getId().equals(id)) {
            throw new AccessDeniedException("Un utilisateur ne peut pas se désactiver lui-même");
        }

        if (utilisateur.getRole() == Role.ADMINISTRATEUR) {
            throw new AccessDeniedException("La désactivation de l'administrateur n'est pas autorisée");
        }

        if (utilisateur.getStatut() == StatutUtilisateur.INACTIF) {
            throw new DeleteUtilisateurImpossibleException("L'utilisateur est déjà inactif");
        }

        if (utilisateur.getStatut() == StatutUtilisateur.ANONYMISE) {
            throw new DeleteUtilisateurImpossibleException("Un utilisateur anonymisé ne peut pas être désactivé");
        }

        authService.forceReturnAllCurrentLoansForUser(utilisateur.getId());

        utilisateur.setStatut(StatutUtilisateur.INACTIF);
        utilisateur.setStatutChangedAt(Instant.now());

        utilisateurRepository.save(utilisateur);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','BIBLIOTHECAIRE')")
    @Transactional
    public void reactivate(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurIsNotExistsException(id));

        authorizationService.checkCanManageUtilisateur(utilisateur);

        if (utilisateur.getStatut() == StatutUtilisateur.ACTIF) {
            return;
        }

        if (utilisateur.getStatut() == StatutUtilisateur.ANONYMISE) {
            throw new DeleteUtilisateurImpossibleException("Un utilisateur anonymisé ne peut pas être réactivé");
        }

        utilisateur.setStatut(StatutUtilisateur.ACTIF);
        utilisateur.setStatutChangedAt(Instant.now());

        utilisateurRepository.save(utilisateur);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','BIBLIOTHECAIRE')")
    @Transactional
    public void anonymize(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurIsNotExistsException(id));

        authorizationService.checkCanManageUtilisateur(utilisateur);

        if (utilisateur.getRole() != Role.ADHERENT) {
            throw new AccessDeniedException("L'anonymisation est réservée aux adhérents");
        }

        if (utilisateur.getStatut() == StatutUtilisateur.ANONYMISE) {
            return;
        }

        authService.forceReturnAllCurrentLoansForUser(utilisateur.getId());

        Long userId = utilisateur.getId();

        utilisateur.setNom("ANONYMISE");
        utilisateur.setPrenom("UTILISATEUR-" + userId);
        utilisateur.setEmail("anonyme-" + userId + "@library.local");
        utilisateur.setMotDePasse(passwordEncoder.encode(UUID.randomUUID().toString()));
        utilisateur.setStatut(StatutUtilisateur.ANONYMISE);
        utilisateur.setStatutChangedAt(Instant.now());

        utilisateurRepository.save(utilisateur);
    }

    private UtilisateurDto.UtilisateurResponse toResponse(Utilisateur utilisateur) {
        return new UtilisateurDto.UtilisateurResponse(
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getEmail(),
                utilisateur.getRole(),
                utilisateur.getStatut()
        );
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
