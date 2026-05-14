package com.chague.bibliotheque.api.dto;

import com.chague.bibliotheque.domain.Role;
import com.chague.bibliotheque.domain.StatutUtilisateur;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class UtilisateurDto {

    public record CreateUtilisateurRequest(
            @NotBlank
            @Size(max = 100)
            String nom,

            @NotBlank
            @Size(max = 100)
            String prenom,

            @NotBlank
            @Email
            @Size(max = 50)
            String email,

            @NotBlank
            @Size(min = 8, max = 255)
            String motDePasse,

            @NotNull
            Role role
    ) {
    }

    public record UpdateUtilisateurRequest(
            @NotBlank
            @Size(max = 100)
            String nom,

            @NotBlank
            @Size(max = 100)
            String prenom,

            @NotBlank
            @Email
            @Size(max = 50)
            String email,

            @NotNull
            Role role
    ) {
    }

    public record UtilisateurResponse(
            Long id,
            String nom,
            String prenom,
            String email,
            Role role,
            StatutUtilisateur statut
    ) {
    }

    public record UtilisateurSummaryResponse(
            Long id,
            String nom,
            String prenom,
            String email,
            Role role,
            StatutUtilisateur statut
    ) {
    }
}
