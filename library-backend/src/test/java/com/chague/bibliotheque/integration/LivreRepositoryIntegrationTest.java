package com.chague.bibliotheque.integration;

import com.chague.bibliotheque.domain.Categorie;
import com.chague.bibliotheque.domain.Langue;
import com.chague.bibliotheque.domain.Livre;
import com.chague.bibliotheque.infrastructure.persistence.CategorieRepository;
import com.chague.bibliotheque.infrastructure.persistence.LivreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Transactional
@DisplayName("LivreRepository sur MySQL reel (Testcontainers)")
class LivreRepositoryIntegrationTest {

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
        // ddl-auto=create force Hibernate a recreer le schema a partir des
        // entites JPA pour chaque execution du test.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private CategorieRepository categorieRepository;

    @Test
    @DisplayName("persiste un livre avec ses categories (relation many-to-many)")
    void persiste_livre_avec_categories() {
        // Noms de categorie uniques par execution pour ne pas entrer en
        // conflit avec les seeds (Roman, Philosophie...) charges au
        // demarrage du contexte Spring par SeedUsersFile / SeedCategorie.
        String suffix = String.valueOf(System.nanoTime());

        Categorie roman = new Categorie();
        roman.setNomCategorie("RomanTest" + suffix);
        roman = categorieRepository.save(roman);

        Categorie philo = new Categorie();
        philo.setNomCategorie("PhiloTest" + suffix);
        philo = categorieRepository.save(philo);

        Livre livre = new Livre();
        livre.setTitre("L'Étranger");
        livre.setAuteur("Albert Camus");
        livre.setLangue(Langue.FR);
        livre.setQuantiteTotale(3);
        livre.setCategories(List.of(roman, philo));

        Livre saved = livreRepository.save(livre);

        assertThat(saved.getId()).isNotNull();

        Livre reloaded = livreRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getTitre()).isEqualTo("L'Étranger");
        assertThat(reloaded.getCategories()).hasSize(2)
                .extracting(Categorie::getNomCategorie)
                .containsExactlyInAnyOrder("RomanTest" + suffix, "PhiloTest" + suffix);
    }

    @Test
    @DisplayName("la methode derivee existsByTitreIgnoreCaseAndAuteurIgnoreCase fonctionne sur MySQL")
    void existsByTitreEtAuteur_insensible_casse() {
        Livre livre = new Livre();
        livre.setTitre("Le Petit Prince");
        livre.setAuteur("Saint-Exupéry");
        livre.setLangue(Langue.FR);
        livre.setQuantiteTotale(1);
        livreRepository.save(livre);

        // Casse differente sur titre + auteur, doit matcher
        boolean exists = livreRepository.existsByTitreIgnoreCaseAndAuteurIgnoreCase(
                "LE PETIT PRINCE", "saint-exupéry"
        );
        assertThat(exists).isTrue();

        // Auteur different, ne doit pas matcher
        boolean notExists = livreRepository.existsByTitreIgnoreCaseAndAuteurIgnoreCase(
                "Le Petit Prince", "Quelqu'un d'autre"
        );
        assertThat(notExists).isFalse();
    }
}
