package com.chague.bibliotheque.api.controller;

import com.chague.bibliotheque.api.dto.MonCompteDto;
import com.chague.bibliotheque.api.dto.UtilisateurDto;
import com.chague.bibliotheque.infrastructure.config.ApiPaths;
import com.chague.bibliotheque.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping(ApiPaths.UTILISATEURS + "/me")
public class MonCompteController {

    private final UtilisateurService utilisateurService;

    @GetMapping
    public UtilisateurDto.UtilisateurResponse getMyProfile() {
        return utilisateurService.getMyProfile();
    }

    @PutMapping
    public UtilisateurDto.UtilisateurResponse updateMyProfile(
            @Valid @RequestBody MonCompteDto.UpdateMyProfileRequest req
    ) {
        return utilisateurService.updateMyProfile(req);
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMyPassword(
            @Valid @RequestBody MonCompteDto.UpdateMyPasswordRequest req
    ) {
        utilisateurService.updateMyPassword(req);
    }
}
