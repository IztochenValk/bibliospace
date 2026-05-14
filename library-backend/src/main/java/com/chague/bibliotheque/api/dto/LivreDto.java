package com.chague.bibliotheque.api.dto;

import com.chague.bibliotheque.domain.Langue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class LivreDto {

    private LivreDto() {
    }

    /**
     * Format ISBN-10 : 9 chiffres suivis d'un caractère de contrôle (0-9 ou X).
     * Une chaîne vide ou {@code null} est acceptée (ISBN facultatif).
     */
    public static final String ISBN10_PATTERN = "^$|^\\d{9}[\\dXx]$";

    public record CreateLivreRequest(
            @NotBlank
            @Size(max = 255)
            String titre,

            @NotBlank
            @Size(max = 255)
            String auteur,

            @Size(max = 1000)
            String description,

            @NotNull
            Langue langue,

            @Size(max = 500)
            String imageUrl,

            @Pattern(regexp = ISBN10_PATTERN, message = "L'ISBN doit contenir 9 chiffres et un caractère de contrôle (0-9 ou X)")
            String isbn,

            @Min(value = 0, message = "La quantité totale ne peut pas être négative")
            Integer quantiteTotale,

            List<Long> categorieIds
    ) {
    }

    public record UpdateLivreRequest(
            @NotBlank
            @Size(max = 255)
            String titre,

            @NotBlank
            @Size(max = 255)
            String auteur,

            @Size(max = 1000)
            String description,

            @NotNull
            Langue langue,

            @Size(max = 500)
            String imageUrl,

            @Pattern(regexp = ISBN10_PATTERN, message = "L'ISBN doit contenir 9 chiffres et un caractère de contrôle (0-9 ou X)")
            String isbn,

            @Min(value = 0, message = "La quantité totale ne peut pas être négative")
            Integer quantiteTotale,

            List<Long> categorieIds
    ) {
    }

    public record LivreResponse(
            Long id,
            String titre,
            String auteur,
            String description,
            Langue langue,
            String imageUrl,
            String isbn,
            Integer quantiteTotale,
            Integer quantiteDisponible,
            List<CategorieDto.CategorieSummaryResponse> categories
    ) {
    }

    public record LivreSummaryResponse(
            Long id,
            String titre,
            String auteur
    ) {
    }
}
