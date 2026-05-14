package com.chague.bibliotheque.api.exception.categorie;

public class CategorieIsNotExistsException extends RuntimeException {
    public CategorieIsNotExistsException(Long id) {
        super("La catégorie ayant pour id: " + id + " n'existe pas");
    }
}
