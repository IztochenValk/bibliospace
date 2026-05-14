package com.chague.bibliotheque.api.exception.livre;

public class LivreIsNotExistsException extends RuntimeException {
    public LivreIsNotExistsException(Long id) {
        super("Le livre ayant pour id: " + id + " n'existe pas");
    }
}
