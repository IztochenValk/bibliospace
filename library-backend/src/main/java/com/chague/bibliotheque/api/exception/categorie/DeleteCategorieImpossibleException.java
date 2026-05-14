package com.chague.bibliotheque.api.exception.categorie;

public class DeleteCategorieImpossibleException extends RuntimeException {
    public DeleteCategorieImpossibleException() {
        super("La suppression est impossible");
    }
}
