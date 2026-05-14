package com.chague.bibliotheque.api.exception.user;

public class UtilisateurIsNotExistsException extends RuntimeException {
    public UtilisateurIsNotExistsException(Long id) {
        super("L'utilisateur ayant pour id: " + id + " n'existe pas");
    }
}
