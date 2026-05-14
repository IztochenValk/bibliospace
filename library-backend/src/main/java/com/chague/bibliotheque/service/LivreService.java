package com.chague.bibliotheque.service;

import com.chague.bibliotheque.api.dto.CategorieDto;
import com.chague.bibliotheque.api.dto.LivreDto;
import com.chague.bibliotheque.api.exception.categorie.CategorieIsNotExistsException;
import com.chague.bibliotheque.api.exception.livre.DeleteLivreImpossibleException;
import com.chague.bibliotheque.api.exception.livre.LivreIsNotExistsException;
import com.chague.bibliotheque.api.exception.livre.LivreIsPresentException;
import com.chague.bibliotheque.domain.Categorie;
import com.chague.bibliotheque.domain.Livre;
import com.chague.bibliotheque.domain.StatutEmprunt;
import com.chague.bibliotheque.infrastructure.persistence.CategorieRepository;
import com.chague.bibliotheque.infrastructure.persistence.EmpruntRepository;
import com.chague.bibliotheque.infrastructure.persistence.LivreRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class LivreService {

    private final LivreRepository livreRepository;
    private final CategorieRepository categorieRepository;
    private final EmpruntRepository empruntRepository;
    private final LivreImageStorageService livreImageStorageService;

    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMINISTRATEUR')")
    @Transactional
    public LivreDto.LivreResponse create(LivreDto.CreateLivreRequest req) {
        if (livreRepository.existsByTitreIgnoreCaseAndAuteurIgnoreCase(req.titre(), req.auteur())) {
            throw new LivreIsPresentException("Un livre avec le même titre et le même auteur existe déjà");
        }

        Livre livre = new Livre();
        livre.setTitre(req.titre());
        livre.setAuteur(req.auteur());
        livre.setDescription(req.description());
        livre.setLangue(req.langue());
        livre.setImageUrl(normalizeStoredImageFilename(req.imageUrl()));
        livre.setIsbn(normalizeIsbn(req.isbn()));
        livre.setQuantiteTotale(req.quantiteTotale() != null ? req.quantiteTotale() : 0);
        livre.setCategories(resolveCategories(req.categorieIds()));

        Livre saved = livreRepository.save(livre);
        return toResponse(saved);
    }

    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMINISTRATEUR')")
    @Transactional
    public LivreDto.LivreResponse update(Long id, LivreDto.UpdateLivreRequest req) {
        Livre livre = livreRepository.findById(id)
                .orElseThrow(() -> new LivreIsNotExistsException(id));

        Integer nouvelleQuantite = req.quantiteTotale() != null ? req.quantiteTotale() : 0;
        long empruntsActifs = empruntRepository.countByLivreIdAndStatut(id, StatutEmprunt.EN_COURS);
        if (nouvelleQuantite < empruntsActifs) {
            throw new DeleteLivreImpossibleException();
        }

        livre.setTitre(req.titre());
        livre.setAuteur(req.auteur());
        livre.setDescription(req.description());
        livre.setLangue(req.langue());
        livre.setImageUrl(normalizeStoredImageFilename(req.imageUrl()));
        livre.setIsbn(normalizeIsbn(req.isbn()));
        livre.setQuantiteTotale(nouvelleQuantite);
        livre.setCategories(resolveCategories(req.categorieIds()));

        Livre saved = livreRepository.save(livre);
        return toResponse(saved);
    }

    /**
     * Consultation de la fiche d'un livre.
     *
     * Ouverte a tous les utilisateurs authentifies : un adherent peut
     * consulter le detail d'un livre depuis le catalogue avant d'emprunter,
     * un membre du staff y accede aussi pour la gestion. La protection
     * (authentification requise, pas d'acces anonyme) est appliquee au
     * niveau de SecurityConfig.anyRequest().authenticated().
     *
     * Les operations sensibles (creation, modification, suppression)
     * restent reservees au staff via @PreAuthorize.
     */
    @Transactional(readOnly = true)
    public LivreDto.LivreResponse getById(Long id) {
        Livre livre = livreRepository.findById(id)
                .orElseThrow(() -> new LivreIsNotExistsException(id));

        return toResponse(livre);
    }

    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMINISTRATEUR')")
    @Transactional(readOnly = true)
    public List<LivreDto.LivreResponse> list() {
        return livreRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMINISTRATEUR')")
    @Transactional
    public void delete(Long id) {
        Livre livre = livreRepository.findById(id)
                .orElseThrow(() -> new LivreIsNotExistsException(id));

        if (empruntRepository.existsByLivreId(id)) {
            throw new DeleteLivreImpossibleException();
        }

        livreRepository.delete(livre);
    }

    /**
     * Calcule la quantité disponible d'un livre = quantité totale moins le nombre
     * d'emprunts en cours sur ce livre. Bornée à 0 (jamais négative) pour rester
     * cohérente même en cas d'incohérence ponctuelle de données.
     *
     * Cette méthode remplace l'ancien StockService (supprimé lors de la fusion
     * Livre + InventaireLivre).
     */
    @Transactional(readOnly = true)
    public int calculateDisponible(Livre livre) {
        long empruntsEnCours = empruntRepository.countByLivreIdAndStatut(
                livre.getId(),
                StatutEmprunt.EN_COURS
        );
        return livre.getQuantiteTotale() - (int)empruntsEnCours;
    }

    private List<Categorie> resolveCategories(List<Long> categorieIds) {
        if (categorieIds == null || categorieIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Categorie> categories = categorieRepository.findAllById(categorieIds);

        if (categories.size() != categorieIds.size()) {
            for (Long categorieId : categorieIds) {
                boolean exists = categories.stream().anyMatch(c -> c.getId().equals(categorieId));
                if (!exists) {
                    throw new CategorieIsNotExistsException(categorieId);
                }
            }
        }

        return categories;
    }

    /**
     * Normalise une saisie ISBN : trim, tirets retirés, clé de contrôle en majuscule.
     * Renvoie {@code null} si la saisie est vide (ISBN facultatif).
     */
    private String normalizeIsbn(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.replace("-", "").trim().toUpperCase();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private LivreDto.LivreResponse toResponse(Livre livre) {
        return new LivreDto.LivreResponse(
                livre.getId(),
                livre.getTitre(),
                livre.getAuteur(),
                livre.getDescription(),
                livre.getLangue(),
                toPublicImageUrl(livre.getImageUrl()),
                livre.getIsbn(),
                livre.getQuantiteTotale(),
                calculateDisponible(livre),
                toCategorieSummaryList(livre.getCategories())
        );
    }

    private List<CategorieDto.CategorieSummaryResponse> toCategorieSummaryList(List<Categorie> categories) {
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }

        return categories.stream()
                .map(c -> new CategorieDto.CategorieSummaryResponse(
                        c.getId(),
                        c.getNomCategorie()
                ))
                .toList();
    }

    private String normalizeStoredImageFilename(String value) {
        return livreImageStorageService.normalizeStoredFilename(value);
    }

    private String toPublicImageUrl(String storedFilename) {
        if (storedFilename == null || storedFilename.isBlank()) {
            return null;
        }
        return "/api/media/livres/" + storedFilename;
    }
}
