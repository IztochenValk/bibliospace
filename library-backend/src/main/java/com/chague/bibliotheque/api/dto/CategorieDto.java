package com.chague.bibliotheque.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CategorieDto {

    private CategorieDto() {
    }

    public record CreateCategorieRequest(
            @NotBlank
            @Size(max = 50)
            String nomCategorie
    ) {
    }

    public record UpdateCategorieRequest(
            @NotBlank
            @Size(max = 50)
            String nomCategorie
    ) {
    }

    public record CategorieResponse(
            Long id,
            String nomCategorie,
            boolean utilisee
    ) {
    }

    public record CategorieSummaryResponse(
            Long id,
            String nomCategorie
    ) {
    }
}
