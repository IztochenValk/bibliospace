package com.chague.bibliotheque.api.controller;

import com.chague.bibliotheque.api.dto.UtilisateurDto;
import com.chague.bibliotheque.domain.Role;
import com.chague.bibliotheque.domain.StatutUtilisateur;
import com.chague.bibliotheque.infrastructure.config.ApiPaths;
import com.chague.bibliotheque.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping(ApiPaths.UTILISATEURS)
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @PutMapping("/{id}")
    public UtilisateurDto.UtilisateurResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UtilisateurDto.UpdateUtilisateurRequest req
    ) {
        return utilisateurService.update(id, req);
    }

    @GetMapping("/{id}")
    public UtilisateurDto.UtilisateurResponse getById(@PathVariable Long id) {
        return utilisateurService.getById(id);
    }

    @GetMapping
    public List<UtilisateurDto.UtilisateurResponse> list() {
        return utilisateurService.list();
    }

    @GetMapping("/role/{role}")
    public List<UtilisateurDto.UtilisateurResponse> listByRole(@PathVariable Role role) {
        return utilisateurService.listByRole(role);
    }

    @GetMapping("/statut/{statut}")
    public List<UtilisateurDto.UtilisateurResponse> listByStatut(@PathVariable StatutUtilisateur statut) {
        return utilisateurService.listByStatut(statut);
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        utilisateurService.deactivate(id);
    }

    @PatchMapping("/{id}/reactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reactivate(@PathVariable Long id) {
        utilisateurService.reactivate(id);
    }

    @PatchMapping("/{id}/anonymize")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void anonymize(@PathVariable Long id) {
        utilisateurService.anonymize(id);
    }
}
