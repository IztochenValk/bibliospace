package com.chague.bibliotheque.api.exception.user;

public class DeleteUtilisateurImpossibleException extends RuntimeException {
    public DeleteUtilisateurImpossibleException() {
        super("La désactivation de l'utilisateur est impossible");
    }

    public DeleteUtilisateurImpossibleException(String message) {
        super(message);
    }
}
