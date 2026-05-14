package com.chague.bibliotheque.api.exception.emprunt;

public class EmpruntIsNotExistsException extends RuntimeException {
    public EmpruntIsNotExistsException(Long id) {
        super("L'emprunt ayant pour id : " + id + " n'existe pas");
    }
}