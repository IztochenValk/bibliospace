package com.chague.bibliotheque.api.dto;

public final class CatalogueDto {

    private CatalogueDto() {
    }

    public record CatalogueItemResponse(
            Long livreId,
            String titre,
            String auteur,
            String description,
            String imageUrl,
            Integer quantiteTotale,
            Integer quantiteDisponible,
            Boolean emprunteParUtilisateur
    ) {
    }
}
