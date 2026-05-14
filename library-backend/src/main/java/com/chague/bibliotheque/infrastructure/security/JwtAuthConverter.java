package com.chague.bibliotheque.infrastructure.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Convertit un Jwt valide (signature + expiration OK) en JwtAuthenticationToken
 * utilisable par Spring Security.
 *
 * Role principal : lire la claim "roles" portee par le token (liste de chaines)
 * et la transformer en GrantedAuthority, pour que @PreAuthorize("hasRole(...)")
 * fonctionne naturellement.
 *
 * Le "name" de l'authentication est le subject (email) : c'est ce que
 * Authentication.getName() renverra dans le reste de l'application.
 *Jwt = token décodé. AbstractAthenticationToken = Objet qui représente l'utilisateur authentifié dans l'appplication
 **/



public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        Collection<GrantedAuthority> authorities = (roles == null ? List.<String>of() : roles)
                .stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }
}
