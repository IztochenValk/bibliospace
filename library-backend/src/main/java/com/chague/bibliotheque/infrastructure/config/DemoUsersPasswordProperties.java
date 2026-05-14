package com.chague.bibliotheque.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Surcharge optionnelle des mots de passe des utilisateurs du seed
 * (bootstrap/users.seed.json), pilotée par variable d'environnement.
 *
 * <p>Ces propriétés sont lues depuis le préfixe {@code app.demo.password.*}.
 * En production, on les définit via les variables d'environnement
 * {@code APP_DEMO_PASSWORD_ADMIN}, {@code APP_DEMO_PASSWORD_BIBLIOTHECAIRE},
 * {@code APP_DEMO_PASSWORD_ADHERENT}, dont la valeur provient des secrets
 * GitHub Actions. Si l'une est définie, elle écrase le mot de passe brut
 * lu dans le fichier JSON pour les utilisateurs du rôle correspondant.</p>
 *
 * <p>En développement local, ces variables ne sont PAS définies : le seeder
 * retombe sur les valeurs présentes dans {@code users.seed.json} (Admin123!
 * et autres) — pratique pour les tests manuels.</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.demo.password")
public class DemoUsersPasswordProperties {

    /** Mot de passe à appliquer à TOUS les utilisateurs de rôle ADMINISTRATEUR. */
    private String admin;

    /** Mot de passe à appliquer à TOUS les utilisateurs de rôle BIBLIOTHECAIRE. */
    private String bibliothecaire;

    /** Mot de passe à appliquer à TOUS les utilisateurs de rôle ADHERENT. */
    private String adherent;
}
