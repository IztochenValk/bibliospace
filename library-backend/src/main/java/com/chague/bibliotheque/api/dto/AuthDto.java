package com.chague.bibliotheque.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDto {

    private AuthDto() {
    }

    public record LoginRequest(
            @NotBlank
            @Email
            String email,

            @NotBlank
            String password
    ) {
    }

    public record RegisterRequest(
            @NotBlank
            @Size(max = 100)
            String nom,

            @NotBlank
            @Size(max = 100)
            String prenom,

            @NotBlank
            @Email
            @Size(max = 150)
            String email,

            @NotBlank
            @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
            String password
    ) {
    }

    public record TokenResponse(
            String token
    ) {
    }
}
