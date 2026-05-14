package com.chague.bibliotheque.api.exception.categorie;

public class CategorieListIsEmptyException extends RuntimeException {
    public CategorieListIsEmptyException() {
        super("La liste des catégories est vide");
    }
}
