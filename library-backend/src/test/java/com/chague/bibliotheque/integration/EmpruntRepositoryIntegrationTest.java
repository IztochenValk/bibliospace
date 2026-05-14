package com.chague.bibliotheque.integration;

import com.chague.bibliotheque.domain.Emprunt;
import com.chague.bibliotheque.domain.Langue;
import com.chague.bibliotheque.domain.Livre;
import com.chague.bibliotheque.domain.Role;
import com.chague.bibliotheque.domain.StatutEmprunt;
import com.chague.bibliotheque.domain.Utilisateur;
import com.chague.bibliotheque.infrastructure.persistence.EmpruntRepository;
import com.chague.bibliotheque.infrastructure.persistence.LivreRepository;
import com.chague.bibliotheque.infrastructure.persistence.UtilisateurRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test d'integration JPA sur EmpruntRepository avec un vrai MySQL via Testcontainers.
 *
 * Cible la requete JPQL custom rechercherParNomEtTitre qui combine deux
 * JOIN (vers utilisateur et livre) et applique LOWER + LIKE pour une
 * recherche insensible a la casse. Ce comportement est sensible a la
 * collation MySQL utf8mb4 et n'est pas reproductible fidelement sur H2,
 * d'ou le choix de Testcontainers.
 */
@Testcontainers
@SpringBootTest
@Transactional
@DisplayName("Integration JPA — EmpruntRepository sur MySQL reel (Testcontainers)")
class EmpruntRepositoryIntegrationTest {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("bibliospace_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerMysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    @Autowired
    private EmpruntRepository empruntRepository;

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Test
    @DisplayName("rechercherParNomEtTitre filtre via JOIN et LOWER+LIKE insensibles a la casse")
    void rechercherParNomEtTitre_filtre_correctement() {
        // ARRANGE : noms / titres tres uniques pour eviter d'entrer en
        // collision avec les seeds (utilisateurs et livres charges au
        // demarrage du contexte Spring via SeedUsersFile).
        String suffix = String.valueOf(System.nanoTime());
        String nomUnique = "DurandTest" + suffix;
        String titreUnique = "TitreUnique" + suffix;

        Utilisateur alice = new Utilisateur();
        alice.setNom(nomUnique);
        alice.setPrenom("Alice");
        alice.setEmail("alice" + suffix + "@example.com");
        alice.setMotDePasse("$2a$10$dummyhash");
        alice.setRole(Role.ADHERENT);
        alice = utilisateurRepository.save(alice);

        Livre livre = new Livre();
        livre.setTitre(titreUnique);
        livre.setAuteur("Albert Camus");
        livre.setLangue(Langue.FR);
        livre.setQuantiteTotale(1);
        livre = livreRepository.save(livre);

        Emprunt emprunt = new Emprunt();
        emprunt.setUtilisateur(alice);
        emprunt.setLivre(livre);
        emprunt.setDateEmprunt(LocalDate.now());
        emprunt.setDateRetourPrevue(LocalDate.now().plusDays(14));
        emprunt.setStatut(StatutEmprunt.EN_COURS);
        empruntRepository.save(emprunt);

        // ACT : recherche avec casse opposee — doit matcher grace au LOWER
        List<Emprunt> resultats = empruntRepository.rechercherParNomEtTitre(
                nomUnique.toUpperCase(),
                titreUnique.toLowerCase()
        );

        // ASSERT : un seul resultat doit matcher (les seeds n'ont ni le
        // nom unique ni le titre unique avec ce suffixe nanoTime).
        // On verifie l'identite exacte de l'emprunt et le bon JOIN
        // (utilisateur + livre correctement rapatries via la JPQL).
        assertThat(resultats)
                .hasSize(1)
                .first()
                .satisfies(e -> {
                    assertThat(e.getUtilisateur().getNom()).isEqualTo(nomUnique);
                    assertThat(e.getLivre().getTitre()).isEqualTo(titreUnique);
                    assertThat(e.getStatut()).isEqualTo(StatutEmprunt.EN_COURS);
                });
    }
}
