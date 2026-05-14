package com.chague.bibliotheque.infrastructure.persistence;

import com.chague.bibliotheque.domain.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategorieRepository extends JpaRepository<Categorie, Long> {

    boolean existsByNomCategorie(String nomCategorie);

    boolean existsByNomCategorieAndIdNot(String nomCategorie, Long id);
}
