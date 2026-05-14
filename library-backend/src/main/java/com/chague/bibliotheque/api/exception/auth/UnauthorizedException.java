package com.chague.bibliotheque.api.exception.auth;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super("Accès non autorisé");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
