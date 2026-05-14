package com.chague.bibliotheque.infrastructure.security;

import lombok.NoArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Helper pour recuperer l'identifiant (userId) de l'utilisateur authentifie
 * depuis le JWT courant.
 *
 * Le JWT emis au login embarque une claim "uid" avec l'identifiant numerique
 * de l'utilisateur. On la lit directement depuis le principal Jwt, sans
 * appel a la base.
 */
@NoArgsConstructor
public final class AuthContext {

    public static Long requireUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("Utilisateur non authentifie");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt jwt) {
            Object uidClaim = jwt.getClaim("uid");
            if (uidClaim instanceof Number number) {
                return number.longValue();
            }
            // Fallback : certains tests peuvent mettre l'id dans le subject.
            try {
                return Long.valueOf(jwt.getSubject());
            } catch (NumberFormatException ignored) {
                throw new AuthenticationCredentialsNotFoundException("Claim uid manquante dans le JWT");
            }
        }

        throw new AuthenticationCredentialsNotFoundException("Principal JWT introuvable");
    }
}
