package com.chague.bibliotheque.api.controller;

import com.chague.bibliotheque.api.dto.EmpruntDto;
import com.chague.bibliotheque.infrastructure.config.ApiPaths;
import com.chague.bibliotheque.service.EmpruntService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping(ApiPaths.EMPRUNTS)
public class EmpruntController {

    private final EmpruntService empruntService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpruntDto.EmpruntResponse create(
            @Valid @RequestBody EmpruntDto.CreateEmpruntRequest req
    ) {
        return empruntService.create(req);
    }

    @PatchMapping("/{id}/retour")
    public EmpruntDto.EmpruntResponse retourner(
            @PathVariable Long id,
            @Valid @RequestBody EmpruntDto.RetourEmpruntRequest req
    ) {
        return empruntService.retourner(id, req);
    }

    @GetMapping("/{id}")
    public EmpruntDto.EmpruntResponse getById(@PathVariable Long id) {
        return empruntService.getById(id);
    }

    @GetMapping
    public List<EmpruntDto.EmpruntResponse> list() {
        return empruntService.list();
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    public List<EmpruntDto.EmpruntResponse> listByUtilisateur(@PathVariable Long utilisateurId) {
        return empruntService.listByUtilisateur(utilisateurId);
    }

    @GetMapping("/statut/{statut}")
    public List<EmpruntDto.EmpruntResponse> listByStatut(@PathVariable String statut) {
        return empruntService.listByStatut(statut);
    }

    /**
     * Recherche transversale d'emprunts pour le staff.
     * Exemple : GET /api/emprunts/recherche?nom=martin&titre=petit+prince
     */
    @GetMapping("/recherche")
    public List<EmpruntDto.EmpruntResponse> rechercher(
            @RequestParam(defaultValue = "") String nom,
            @RequestParam(defaultValue = "") String titre
    ) {
        return empruntService.rechercher(nom, titre);
    }
}
