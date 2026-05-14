package com.chague.bibliotheque.api.exception.livre;

public class LivreIsPresentException extends RuntimeException {
    public LivreIsPresentException(String message) {
        super(message != null ? message : "Le livre existe déjà");
    }
}