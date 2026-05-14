package com.chague.bibliotheque.api.controller;

import com.chague.bibliotheque.api.dto.AuthDto;
import com.chague.bibliotheque.infrastructure.config.ApiPaths;
import com.chague.bibliotheque.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expose deux endpoints publics :
 *  - POST /api/auth/login    : authentification d'un utilisateur existant
 *  - POST /api/auth/register : inscription auto-service (creation d'un compte ADHERENT)
 *
 * Le client envoie email + mot de passe, recoit un JWT signe RSA en retour,
 * et le joint en header Authorization: Bearer <token> pour toutes les requetes
 * suivantes. Spring Security (oauth2ResourceServer) valide le token tout seul.
 */
@AllArgsConstructor
@RestController
@RequestMapping(ApiPaths.AUTH)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthDto.TokenResponse login(@Valid @RequestBody AuthDto.LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDto.TokenResponse register(@Valid @RequestBody AuthDto.RegisterRequest request) {
        return authService.register(request);
    }
}
