package com.chague.bibliotheque.api.exception.emprunt;

public class EmpruntImpossibleException extends RuntimeException {
    public EmpruntImpossibleException() {
        super("L'emprunt est impossible");
    }

    public EmpruntImpossibleException(String message) {
        super(message);
    }
}