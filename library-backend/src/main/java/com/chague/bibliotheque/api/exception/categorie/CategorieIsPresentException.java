package com.chague.bibliotheque.api.exception.categorie;

public class CategorieIsPresentException extends RuntimeException {
    public CategorieIsPresentException(String nom) {
        super("La catégorie avec le nom : " + nom + " existe déjà");
    }
}
