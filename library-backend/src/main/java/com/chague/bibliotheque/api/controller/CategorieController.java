package com.chague.bibliotheque.api.controller;

import java.util.List;

import com.chague.bibliotheque.infrastructure.config.ApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.chague.bibliotheque.api.dto.CategorieDto;
import com.chague.bibliotheque.service.CategorieService;
import jakarta.validation.Valid;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping(ApiPaths.CATEGORIES)
public class CategorieController {

    private final CategorieService categorieService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategorieDto.CategorieResponse create(@Valid @RequestBody CategorieDto.CreateCategorieRequest req) {
        return categorieService.create(req);
    }

    @GetMapping
    public List<CategorieDto.CategorieResponse> list() {
        return categorieService.list();
    }

    @GetMapping("/{id}")
    public CategorieDto.CategorieResponse getById(@PathVariable Long id) {
        return categorieService.getById(id);
    }

    @PutMapping("/{id}")
    public CategorieDto.CategorieResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CategorieDto.UpdateCategorieRequest req
    ) {
        return categorieService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categorieService.delete(id);
    }
}
