package com.chague.bibliotheque.infrastructure.security;

import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Charge la paire de cles RSA depuis des fichiers PEM.
 *
 * La cle privee sert a signer les JWT emis au login.
 * La cle publique sert a verifier la signature des JWT recus.
 *
 * Cette separation est le fondement du modele asymetrique : un attaquant
 * qui recupere la cle publique ne peut PAS forger de token valide.

 * properties.getPublicKey/PrivateKey() --> Récupère la clé correspondante depuis le .pem
 * decodePem --> Nettoyage des clés. Supprime les mentions -----BEGIN PUBLIC KEY-----, -----END PUBLIC KEY-----;,
 * supprime les retours à la ligne, convertir le base64 en byte[] pur.
 * new X509EncodedKeySpec(...) --> crée un objet JAVA basé sur la clé néttoyée en précisant une spécification (ici, X509)
 * KeyFactory.getInstance("RSA") --> "Je veux construire une clé RSA à partir de ces données."
 * RSAPublicKey --> Cast clé encodée vers RSAPublicKyy. On construit un objet RSAPublicKey exploitable par JAVA
 **/

@Component
@AllArgsConstructor
public class PemKeyLoader {

    private final JwtProperties properties;


    public RSAPublicKey loadPublicKey() throws Exception {
        return (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decodePem(properties.getPublicKey())));
    }

    public RSAPrivateKey loadPrivateKey() throws Exception {
        return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(decodePem(properties.getPrivateKey())));
    }

    private byte[] decodePem(Resource pemResource) throws Exception {
        String pem = new String(pemResource.getInputStream().readAllBytes());
        String base64Body = pem
                .replaceAll("-----.*?-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64Body);
    }
}
