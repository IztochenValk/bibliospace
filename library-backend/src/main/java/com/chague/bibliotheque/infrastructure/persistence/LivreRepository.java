package com.chague.bibliotheque.infrastructure.persistence;

import com.chague.bibliotheque.domain.Livre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LivreRepository extends JpaRepository<Livre, Long> {

    boolean existsByTitreIgnoreCaseAndAuteurIgnoreCase(String titre, String auteur);

    List<Livre> findByTitreContainingIgnoreCase(String titre);

    List<Livre> findByAuteurContainingIgnoreCase(String auteur);

    List<Livre> findByTitreContainingIgnoreCaseOrAuteurContainingIgnoreCase(String titre, String auteur);
}