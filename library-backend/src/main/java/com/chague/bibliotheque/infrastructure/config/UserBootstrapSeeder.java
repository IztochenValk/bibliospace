package com.chague.bibliotheque.infrastructure.config;

import com.chague.bibliotheque.domain.Role;
import com.chague.bibliotheque.domain.StatutUtilisateur;
import com.chague.bibliotheque.domain.Utilisateur;
import com.chague.bibliotheque.infrastructure.persistence.UtilisateurRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserBootstrapSeeder implements CommandLineRunner {

    private static final String SEED_FILE_PATH = "bootstrap/users.seed.json";

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final DemoUsersPasswordProperties demoPasswords;

    @Override
    @Transactional
    public void run(String... args) {
        List<SeedUserItem> users = loadSeedUsers();

        if (users.isEmpty()) {
            log.warn("Bootstrap users skipped: no users found in seed file");
            return;
        }

        int createdCount = 0;
        int skippedCount = 0;

        for (SeedUserItem item : users) {
            if (!isValid(item)) {
                log.warn("Bootstrap user skipped: invalid entry for email={}", item != null ? item.getEmail() : null);
                skippedCount++;
                continue;
            }

            String normalizedEmail = item.getEmail().trim().toLowerCase();

            if (utilisateurRepository.existsByEmail(normalizedEmail)) {
                log.info("Bootstrap user skipped: already exists email={}", normalizedEmail);
                skippedCount++;
                continue;
            }

            Instant now = Instant.now();
            StatutUtilisateur statut = item.getStatut() != null
                    ? item.getStatut()
                    : StatutUtilisateur.ACTIF;

            String rawPassword = resolvePassword(item.getRole(), item.getPassword());

            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setNom(item.getNom().trim());
            utilisateur.setPrenom(item.getPrenom().trim());
            utilisateur.setEmail(normalizedEmail);
            utilisateur.setMotDePasse(passwordEncoder.encode(rawPassword));
            utilisateur.setRole(item.getRole());
            utilisateur.setStatut(statut);
            utilisateur.setStatutChangedAt(now);

            utilisateurRepository.save(utilisateur);
            createdCount++;

            log.info(
                    "Bootstrap user created: email={}, role={}, statut={}",
                    utilisateur.getEmail(),
                    utilisateur.getRole(),
                    utilisateur.getStatut()
            );
        }

        log.info("Bootstrap users completed: created={}, skipped={}", createdCount, skippedCount);
    }

    private List<SeedUserItem> loadSeedUsers() {
        try {
            ClassPathResource resource = new ClassPathResource(SEED_FILE_PATH);

            if (!resource.exists()) {
                log.warn("Bootstrap users skipped: seed file not found at {}", SEED_FILE_PATH);
                return List.of();
            }

            try (InputStream inputStream = resource.getInputStream()) {
                SeedUsersFile file = objectMapper.readValue(inputStream, SeedUsersFile.class);
                return file != null && file.getUsers() != null ? file.getUsers() : List.of();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de charger le fichier de seed des utilisateurs", e);
        }
    }

    private boolean isValid(SeedUserItem item) {
        return item != null
                && notBlank(item.getNom())
                && notBlank(item.getPrenom())
                && notBlank(item.getEmail())
                && notBlank(item.getPassword())
                && item.getRole() != null;
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * Détermine le mot de passe brut à hasher pour un user de seed.
     *
     * <p>Si une variable d'environnement est définie pour le rôle
     * (ex : {@code APP_DEMO_PASSWORD_ADMIN}), elle prend le pas sur le mot
     * de passe lu dans le fichier {@code users.seed.json}. Cela permet de
     * conserver des credentials lisibles en dev tout en injectant des
     * mots de passe forts en production via GitHub Secrets.</p>
     */
    private String resolvePassword(Role role, String fallbackFromJson) {
        String override = switch (role) {
            case ADMINISTRATEUR -> demoPasswords.getAdmin();
            case BIBLIOTHECAIRE -> demoPasswords.getBibliothecaire();
            case ADHERENT       -> demoPasswords.getAdherent();
        };

        if (notBlank(override)) {
            return override;
        }

        return fallbackFromJson;
    }
}
