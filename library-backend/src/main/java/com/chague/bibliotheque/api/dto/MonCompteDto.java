package com.chague.bibliotheque.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class MonCompteDto {

    private MonCompteDto() {
    }

    public record UpdateMyProfileRequest(
            @NotBlank
            @Size(max = 100)
            String nom,

            @NotBlank
            @Size(max = 100)
            String prenom,

            @NotBlank
            @Email
            @Size(max = 50)
            String email
    ) {
    }

    public record UpdateMyPasswordRequest(
            @NotBlank
            @Size(min = 8, max = 255)
            String ancienMotDePasse,

            @NotBlank
            @Size(min = 8, max = 255)
            String nouveauMotDePasse
    ) {
    }
}
