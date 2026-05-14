package com.chague.bibliotheque.api.exception.livre;

public class LivreListIsEmptyException extends RuntimeException {
    public LivreListIsEmptyException() {
        super("La liste des livres est vide");
    }
}
