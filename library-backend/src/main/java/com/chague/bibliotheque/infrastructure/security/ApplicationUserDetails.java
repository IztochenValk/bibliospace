package com.chague.bibliotheque.infrastructure.security;

import com.chague.bibliotheque.domain.StatutUtilisateur;
import com.chague.bibliotheque.domain.Utilisateur;
import com.chague.bibliotheque.infrastructure.persistence.UtilisateurRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * UserDetailsService charge depuis la base de donnees.
 *
 * Utilise par l'AuthenticationManager lors de l'appel a
 * authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(...))
 * dans AuthService.login().
 *
 * Une fois l'utilisateur authentifie, on n'utilise plus ce service :
 * la suite de la vie de la session repose sur le JWT et le JwtDecoder.
 */
public class ApplicationUserDetails implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    public ApplicationUserDetails(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));

        String authority = "ROLE_" + utilisateur.getRole().name();

        return User.withUsername(utilisateur.getEmail())
                .password(utilisateur.getMotDePasse())
                .disabled(utilisateur.getStatut() != StatutUtilisateur.ACTIF)
                .authorities(authority)
                .build();
    }
}
