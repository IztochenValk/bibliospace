package com.chague.bibliotheque.infrastructure.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * Expose les deux beans utilises par Spring Security OAuth2 Resource Server :
 *
 *  - JwtDecoder : verifie la signature et l'expiration d'un JWT entrant,
 *                 avec la cle publique uniquement.
 *
 *  - JwtEncoder : signe un nouveau JWT au login, avec la paire de cles RSA.
 *
 * Ces beans s'integrent automatiquement dans la chaine de filtres Spring
 * lorsque la configuration HTTP active oauth2ResourceServer(jwt -> ...).
 */
@Configuration
@AllArgsConstructor
public class JwtCryptoConfig {

    private final PemKeyLoader pemKeyLoader;

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        RSAPublicKey publicKey = pemKeyLoader.loadPublicKey();
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    @Bean
    public JwtEncoder jwtEncoder() throws Exception {
        RSAPublicKey publicKey = pemKeyLoader.loadPublicKey();
        RSAPrivateKey privateKey = pemKeyLoader.loadPrivateKey();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();

        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
        return new NimbusJwtEncoder(jwkSource);
    }
}
