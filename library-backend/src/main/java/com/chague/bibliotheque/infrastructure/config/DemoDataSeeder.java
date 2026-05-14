package com.chague.bibliotheque.infrastructure.config;

import com.chague.bibliotheque.domain.Categorie;
import com.chague.bibliotheque.domain.Emprunt;
import com.chague.bibliotheque.domain.Livre;
import com.chague.bibliotheque.domain.Role;
import com.chague.bibliotheque.domain.StatutEmprunt;
import com.chague.bibliotheque.domain.StatutUtilisateur;
import com.chague.bibliotheque.domain.Utilisateur;
import com.chague.bibliotheque.infrastructure.persistence.CategorieRepository;
import com.chague.bibliotheque.infrastructure.persistence.EmpruntRepository;
import com.chague.bibliotheque.infrastructure.persistence.LivreRepository;
import com.chague.bibliotheque.infrastructure.persistence.UtilisateurRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemoDataSeeder implements CommandLineRunner {

    private static final String BOOKS_SEED_FILE_PATH = "bootstrap/livres.seed.json";
    private static final String BOOTSTRAP_IMAGES_DIR = "bootstrap/images";

    private final CategorieRepository categorieRepository;
    private final LivreRepository livreRepository;
    private final EmpruntRepository empruntRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ObjectMapper objectMapper;
    private final FileStorageProperties fileStorageProperties;

    private final Random random = new Random(42);

    @Override
    @Transactional
    public void run(String... args) {
        copyBootstrapImagesToUploadDir();
        seedCategoriesIfNeeded();
        seedBooksIfNeeded();
        seedLoansIfNeeded();
    }

    private void seedCategoriesIfNeeded() {
        if (categorieRepository.count() > 0) {
            log.info("Demo categories skipped: already present");
            return;
        }

        List<String> noms = List.of(
                "Roman",
                "Science-fiction",
                "Histoire",
                "Informatique",
                "Cybersécurité",
                "Philosophie",
                "Jeunesse",
                "Biographie"
        );

        List<Categorie> categories = noms.stream().map(nom -> {
            Categorie c = new Categorie();
            c.setNomCategorie(nom);
            return c;
        }).toList();

        categorieRepository.saveAll(categories);
        log.info("Demo categories created: {}", categories.size());
    }

    private void seedBooksIfNeeded() {
        if (livreRepository.count() > 0) {
            log.info("Demo books skipped: already present");
            return;
        }

        List<Categorie> categories = categorieRepository.findAll();
        List<SeedBookItem> items = loadSeedBooks();

        if (items.isEmpty()) {
            log.warn("Demo books skipped: no books found in seed file {}", BOOKS_SEED_FILE_PATH);
            return;
        }

        int created = 0;

        for (SeedBookItem item : items) {
            if (!isValidBook(item)) {
                log.warn(
                        "Demo book skipped: invalid entry titre={}, auteur={}",
                        item != null ? item.getTitre() : null,
                        item != null ? item.getAuteur() : null
                );
                continue;
            }

            Livre livre = new Livre();
            livre.setTitre(item.getTitre().trim());
            livre.setAuteur(item.getAuteur().trim());
            livre.setDescription(item.getDescription());
            livre.setLangue(item.getLangue());
            livre.setImageUrl(normalizeImageFilename(item.getImageUrl()));
            livre.setIsbn(normalizeIsbn(item.getIsbn()));
            livre.setQuantiteTotale(2 + random.nextInt(5));
            livre.setCategories(resolveCategoriesByName(item.getCategories(), categories));

            livreRepository.save(livre);
            created++;
        }

        log.info("Demo books created from JSON: {}", created);
    }

    private void seedLoansIfNeeded() {
        if (empruntRepository.count() > 0) {
            log.info("Demo loans skipped: already present");
            return;
        }

        List<Utilisateur> adherentsActifs = utilisateurRepository.findByRoleAndStatut(
                Role.ADHERENT,
                StatutUtilisateur.ACTIF
        );

        List<Livre> livres = livreRepository.findAll();

        if (adherentsActifs.isEmpty() || livres.isEmpty()) {
            log.info("Demo loans skipped: missing adherents or books");
            return;
        }

        int created = 0;

        for (Livre livre : livres) {
            int stock = livre.getQuantiteTotale() != null ? livre.getQuantiteTotale() : 0;
            if (stock <= 0) {
                continue;
            }

            int nbLoansForThisBook = random.nextInt(stock + 1);
            List<Utilisateur> shuffledUsers = new ArrayList<>(adherentsActifs);
            Collections.shuffle(shuffledUsers, random);

            int max = Math.min(nbLoansForThisBook, shuffledUsers.size());

            for (int i = 0; i < max; i++) {
                Utilisateur user = shuffledUsers.get(i);

                Emprunt emprunt = new Emprunt();
                emprunt.setUtilisateur(user);
                emprunt.setLivre(livre);

                LocalDate dateEmprunt = LocalDate.now().minusDays(random.nextInt(60) + 1);
                boolean returned = random.nextBoolean();

                emprunt.setDateEmprunt(dateEmprunt);
                emprunt.setDateRetourPrevue(dateEmprunt.plusDays(14));

                if (returned) {
                    LocalDate retour = dateEmprunt.plusDays(random.nextInt(20) + 1);
                    emprunt.setDateRetourEffective(retour);
                    emprunt.setStatut(StatutEmprunt.RETOURNE);
                } else {
                    emprunt.setDateRetourEffective(null);
                    emprunt.setStatut(StatutEmprunt.EN_COURS);
                }

                empruntRepository.save(emprunt);
                created++;
            }
        }

        log.info("Demo loans created: {}", created);
    }

    private void copyBootstrapImagesToUploadDir() {
        try {
            Path uploadDir = Path.of(fileStorageProperties.getUploadDir()).normalize().toAbsolutePath();
            Files.createDirectories(uploadDir);

            String[] filenames = {
                    "1984.jpg",
                    "brave-new-world.jpg",
                    "candide.jpg",
                    "clean-code.jpg",
                    "cybersecurity-essentials.jpg",
                    "discovering-cybersecurity.jpg",
                    "don-quichotte.jpg",
                    "dune.jpg",
                    "foundation.jpg",
                    "harry-potter.jpg",
                    "history-of-time.jpg",
                    "l-art-de-la-guerre.jpg",
                    "l-etranger.jpg",
                    "le-petit-prince.jpg",
                    "les-miserables.jpg",
                    "monte-cristo.jpg",
                    "pragmatic-programmer.jpg",
                    "sapiens.jpg",
                    "steve-jobs.jpg"
            };

            for (String filename : filenames) {
                ClassPathResource resource = new ClassPathResource(BOOTSTRAP_IMAGES_DIR + "/" + filename);

                if (!resource.exists()) {
                    log.warn("Bootstrap image not found: {}", filename);
                    continue;
                }

                Path target = uploadDir.resolve(filename).normalize();

                try (InputStream inputStream = resource.getInputStream()) {
                    Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
                }

                log.info("Bootstrap image copied: {}", filename);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de copier les images bootstrap vers le dossier d'upload", e);
        }
    }

    private List<SeedBookItem> loadSeedBooks() {
        try {
            ClassPathResource resource = new ClassPathResource(BOOKS_SEED_FILE_PATH);

            if (!resource.exists()) {
                log.warn("Books seed skipped: file not found at {}", BOOKS_SEED_FILE_PATH);
                return List.of();
            }

            try (InputStream inputStream = resource.getInputStream()) {
                SeedBooksFile file = objectMapper.readValue(inputStream, SeedBooksFile.class);
                return file != null && file.getLivres() != null ? file.getLivres() : List.of();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de charger le fichier de seed des livres", e);
        }
    }

    private boolean isValidBook(SeedBookItem item) {
        return item != null
                && notBlank(item.getTitre())
                && notBlank(item.getAuteur())
                && item.getLangue() != null;
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private List<Categorie> resolveCategoriesByName(List<String> names, List<Categorie> categories) {
        if (names == null || names.isEmpty()) {
            return List.of();
        }

        return categories.stream()
                .filter(c -> names.contains(c.getNomCategorie()))
                .toList();
    }

    private String normalizeIsbn(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.replace("-", "").trim().toUpperCase();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private String normalizeImageFilename(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();
        int slash = normalized.lastIndexOf('/');
        if (slash >= 0) {
            normalized = normalized.substring(slash + 1);
        }

        return normalized;
    }
}
