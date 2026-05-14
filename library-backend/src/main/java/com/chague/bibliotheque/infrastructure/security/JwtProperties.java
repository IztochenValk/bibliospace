/**Le paramètre (prefix = "security.jwt") fait référence à la vairable d'environnement security.jwt.
 *Cette variable d'environnement est injectée dans le contexte Spring par applications.properties. Elle contient
 *plusieurs propriétés, qu'on va retrouver ci-dessous en tant que propriétés de la classe JwtProperties.
 *Par exemple : la propriété "publicKey" correspond à security.jwt.public-key dans application.properties.
 *Pour synthétiser, cette classe contient toutes les propriétés qu'on retrouvera dans le claim de l'objet JwtClaimsSet.
 **/

package com.chague.bibliotheque.infrastructure.security;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;


@Getter
@Setter
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /** PEM contenant la cle publique RSA (verification signature). */
    private Resource publicKey;

    /** PEM contenant la cle privee RSA (signature des tokens emis). */
    private Resource privateKey;

    /** Nom de l'API, stocke dans la claim "iss" du JWT. */
    private String issuer = "app-library";

    /** Durée de vie de l'access token, en minutes. */
    private long expirationMinutes = 60;
}
