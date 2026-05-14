package com.chague.bibliotheque.api.exception.user;

public class UtilisateurIsPresentException extends RuntimeException {
    public UtilisateurIsPresentException(String email) {
        super("Un utilisateur avec l'email : " + email + " existe déjà");
    }
}
