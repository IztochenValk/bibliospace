package com.chague.bibliotheque.service;

import com.chague.bibliotheque.api.dto.AuthDto;
import com.chague.bibliotheque.api.exception.auth.InvalidCredentialsException;
import com.chague.bibliotheque.api.exception.user.UtilisateurIsPresentException;
import com.chague.bibliotheque.domain.Emprunt;
import com.chague.bibliotheque.domain.Role;
import com.chague.bibliotheque.domain.StatutEmprunt;
import com.chague.bibliotheque.domain.StatutUtilisateur;
import com.chague.bibliotheque.domain.Utilisateur;
import com.chague.bibliotheque.infrastructure.persistence.EmpruntRepository;
import com.chague.bibliotheque.infrastructure.persistence.UtilisateurRepository;
import com.chague.bibliotheque.infrastructure.security.JwtProperties;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

/**
 * Emission des JWT.
 *
 * Etapes du login :
 *  1. authenticationManager valide email + mot de passe contre la base
 *     (via ApplicationUserDetails + PasswordEncoder).
 *  2. on lit les authorities de l'Authentication resultante pour construire
 *     la claim "roles".DaoAuthenticationProvider
 *  3. on construit le JwtClaimsSet (iss, sub, iat, exp, uid, roles).
 *  4. jwtEncoder signe le tout avec la cle privee RSA.
 *  5. on retourne le token brut au client.
 */
@AllArgsConstructor
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final UtilisateurRepository utilisateurRepository;
    private final EmpruntRepository empruntRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthDto.TokenResponse login(AuthDto.@NonNull LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        // 1. Authentification via AuthenticationManager (BCrypt + DB lookup)
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException();
        }

        // 2. L'utilisateur est authentifié : on recupere l'entité pour acceder a l'id
        //On doit aussi s'assurer que l'utilisateur a bien le statut "ACTIF".
        Utilisateur utilisateur = utilisateurRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        // 3. Extraction des authorities (ex. ROLE_ADMINISTRATEUR) pour la claim roles
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(now.plus(jwtProperties.getExpirationMinutes(), ChronoUnit.MINUTES))
                .claim("uid", utilisateur.getId())
                .claim("roles", roles)
                .build();

        // 4. Encodage + signature RSA par jwtEncoder
        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return new AuthDto.TokenResponse(token);
    }

    /**
     * Inscription auto-service ouverte aux visiteurs.
     *
     * Etapes :
     *  1. Normalisation de l'email (trim + lowercase)
     *  2. Verification d'unicite (rejet si l'email est deja pris)
     *  3. Création d'un Utilisateur avec rôle ADHERENT, statut ACTIF, mot de passe hash BCrypt
     *  4. Persistance
     *  5. Auto-login : on réutilise login(...) pour émettre un JWT et retourner directement
     *     un TokenResponse au client
     *
     */
    @Transactional
    public AuthDto.TokenResponse register(AuthDto.@NonNull RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        if (utilisateurRepository.existsByEmail(normalizedEmail)) {
            throw new UtilisateurIsPresentException(normalizedEmail);
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(request.nom().trim());
        utilisateur.setPrenom(request.prenom().trim());
        utilisateur.setEmail(normalizedEmail);
        utilisateur.setMotDePasse(passwordEncoder.encode(request.password()));
        utilisateur.setRole(Role.ADHERENT);
        utilisateur.setStatut(StatutUtilisateur.ACTIF);
        utilisateurRepository.save(utilisateur);

        return login(new AuthDto.LoginRequest(normalizedEmail, request.password()));
    }

    /**
     * Rappel : méthode métier conservée pour les changements de statut d'utilisateur
     * (anonymisation, desactivation, changement de role). Force le retour des
     * emprunts en cours côté domaine metier.
     */
    @Transactional
    public void forceReturnAllCurrentLoansForUser(Long utilisateurId) {
        List<Emprunt> empruntsEnCours = empruntRepository.findByUtilisateurIdAndStatut(
                utilisateurId,
                StatutEmprunt.EN_COURS
        );

        LocalDate today = LocalDate.now();

        for (Emprunt emprunt : empruntsEnCours) {
            emprunt.setStatut(StatutEmprunt.RETOURNE);
            if (emprunt.getDateRetourEffective() == null) {
                emprunt.setDateRetourEffective(today);
            }
        }

        empruntRepository.saveAll(empruntsEnCours);
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
