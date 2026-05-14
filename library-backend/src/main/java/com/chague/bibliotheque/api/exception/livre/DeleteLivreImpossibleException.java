package com.chague.bibliotheque.api.exception.livre;

public class DeleteLivreImpossibleException extends RuntimeException {
    public DeleteLivreImpossibleException() {
        super("La suppression du livre est impossible car il est encore lié à un inventaire ou à des emprunts");
    }
}
