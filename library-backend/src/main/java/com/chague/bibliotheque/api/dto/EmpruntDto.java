package com.chague.bibliotheque.api.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import com.chague.bibliotheque.domain.StatutEmprunt;

public final class EmpruntDto {

    private EmpruntDto() {}

    public record CreateEmpruntRequest(
            @NotNull
            Long livreId,

            @NotNull
            @FutureOrPresent
            LocalDate dateRetourPrevue
    ) {}

    public record RetourEmpruntRequest(
            LocalDate dateRetourEffective
    ) {}

    public record EmpruntResponse(
            Long id,
            Long utilisateurId,
            String nomUtilisateur,
            String prenomUtilisateur,
            Long livreId,
            String titreLivre,
            String imageUrl,
            LocalDate dateEmprunt,
            LocalDate dateRetourPrevue,
            LocalDate dateRetourEffective,
            StatutEmprunt statut
    ) {}
}
