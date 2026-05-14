package com.chague.bibliotheque.api.controller;

import com.chague.bibliotheque.api.dto.LivreDto;
import com.chague.bibliotheque.infrastructure.config.ApiPaths;
import com.chague.bibliotheque.service.LivreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping(ApiPaths.LIVRES)
public class LivreController {

    private final LivreService livreService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LivreDto.LivreResponse create(@Valid @RequestBody LivreDto.CreateLivreRequest req) {
        return livreService.create(req);
    }

    @PutMapping("/{id}")
    public LivreDto.LivreResponse update(
            @PathVariable Long id,
            @Valid @RequestBody LivreDto.UpdateLivreRequest req
    ) {
        return livreService.update(id, req);
    }

    @GetMapping("/{id}")
    public LivreDto.LivreResponse getById(@PathVariable Long id) {
        return livreService.getById(id);
    }

    @GetMapping
    public List<LivreDto.LivreResponse> list() {
        return livreService.list();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        livreService.delete(id);
    }
}