package com.chague.bibliotheque.api.exception.user;

public class UtilisateurListIsEmptyException extends RuntimeException {
    public UtilisateurListIsEmptyException() {
        super("La liste des utilisateurs est vide");
    }
}
