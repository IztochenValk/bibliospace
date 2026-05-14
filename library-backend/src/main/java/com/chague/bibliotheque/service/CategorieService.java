package com.chague.bibliotheque.service;

import com.chague.bibliotheque.api.dto.CategorieDto;
import com.chague.bibliotheque.api.exception.categorie.CategorieIsNotExistsException;
import com.chague.bibliotheque.api.exception.categorie.CategorieIsPresentException;
import com.chague.bibliotheque.api.exception.categorie.DeleteCategorieImpossibleException;
import com.chague.bibliotheque.domain.Categorie;
import com.chague.bibliotheque.infrastructure.persistence.CategorieRepository;
import com.chague.bibliotheque.infrastructure.persistence.LivreRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
public class CategorieService {

    private final CategorieRepository categorieRepository;
    private final LivreRepository livreRepository;

    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMINISTRATEUR')")
    @Transactional
    public CategorieDto.CategorieResponse create(CategorieDto.CreateCategorieRequest req) {
        String nom = normalizeNom(req.nomCategorie());

        if (categorieRepository.existsByNomCategorie(nom)) {
            throw new CategorieIsPresentException(nom);
        }

        Categorie categorie = new Categorie();
        categorie.setNomCategorie(nom);

        Categorie saved = categorieRepository.save(categorie);

        return toResponse(saved);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<CategorieDto.CategorieResponse> list() {
        return categorieRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public CategorieDto.CategorieResponse getById(Long id) {
        Categorie categorie = categorieRepository.findById(id)
                .orElseThrow(() -> new CategorieIsNotExistsException(id));

        return toResponse(categorie);
    }

    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMINISTRATEUR')")
    @Transactional
    public CategorieDto.CategorieResponse update(Long id, CategorieDto.UpdateCategorieRequest req) {
        Categorie categorie = categorieRepository.findById(id)
                .orElseThrow(() -> new CategorieIsNotExistsException(id));

        String nom = normalizeNom(req.nomCategorie());

        if (categorieRepository.existsByNomCategorieAndIdNot(nom, id)) {
            throw new CategorieIsPresentException(nom);
        }

        categorie.setNomCategorie(nom);

        Categorie saved = categorieRepository.save(categorie);
        return toResponse(saved);
    }

    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMINISTRATEUR')")
    @Transactional
    public void delete(Long id) {
        Categorie categorie = categorieRepository.findById(id)
                .orElseThrow(() -> new CategorieIsNotExistsException(id));

        if (isCategorieUsed(id)) {
            throw new DeleteCategorieImpossibleException();
        }

        categorieRepository.delete(categorie);
    }

    private CategorieDto.CategorieResponse toResponse(Categorie categorie) {
        return new CategorieDto.CategorieResponse(
                categorie.getId(),
                categorie.getNomCategorie(),
                isCategorieUsed(categorie.getId())
        );
    }

    private boolean isCategorieUsed(Long categorieId) {
        return livreRepository.findAll().stream()
                .anyMatch(livre ->
                        livre.getCategories() != null &&
                        livre.getCategories().stream()
                                .anyMatch(cat -> cat.getId().equals(categorieId))
                );
    }

    private String normalizeNom(String nomCategorie) {
        if (nomCategorie == null) {
            return null;
        }

        return nomCategorie.trim();
    }
}
