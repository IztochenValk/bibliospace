package com.chague.bibliotheque.api.exception.emprunt;

public class EmpruntListIsEmptyException extends RuntimeException {
    public EmpruntListIsEmptyException() {
        super("La liste des emprunts est vide");
    }
}