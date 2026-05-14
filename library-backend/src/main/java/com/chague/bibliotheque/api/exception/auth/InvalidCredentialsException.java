package com.chague.bibliotheque.api.exception.auth;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Identifiants invalides");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
