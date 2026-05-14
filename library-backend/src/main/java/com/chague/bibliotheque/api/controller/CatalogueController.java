package com.chague.bibliotheque.api.controller;

import com.chague.bibliotheque.api.dto.CatalogueDto;
import com.chague.bibliotheque.infrastructure.config.ApiPaths;
import com.chague.bibliotheque.service.CatalogueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.CATALOGUE)
@RequiredArgsConstructor
public class CatalogueController {

    private final CatalogueService catalogueService;

    @GetMapping
    public List<CatalogueDto.CatalogueItemResponse> getCatalogue() {
        return catalogueService.listCatalogue();
    }
}
