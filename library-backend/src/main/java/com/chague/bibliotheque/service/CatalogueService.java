package com.chague.bibliotheque.service;

import com.chague.bibliotheque.api.dto.CatalogueDto;
import com.chague.bibliotheque.api.exception.auth.UnauthorizedException;
import com.chague.bibliotheque.domain.Livre;
import com.chague.bibliotheque.domain.Role;
import com.chague.bibliotheque.domain.StatutEmprunt;
import com.chague.bibliotheque.domain.Utilisateur;
import com.chague.bibliotheque.infrastructure.persistence.EmpruntRepository;
import com.chague.bibliotheque.infrastructure.persistence.LivreRepository;
import com.chague.bibliotheque.infrastructure.persistence.UtilisateurRepository;
import com.chague.bibliotheque.infrastructure.security.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogueService {

    private final UtilisateurRepository utilisateurRepository;
    private final LivreRepository livreRepository;
    private final EmpruntRepository empruntRepository;
    private final LivreService livreService;

    @Transactional(readOnly = true)
    public List<CatalogueDto.CatalogueItemResponse> listCatalogue() {
        Utilisateur utilisateur = requireCurrentUtilisateur();

        return livreRepository.findAll().stream()
                .map(livre -> toCatalogueItemResponse(livre, utilisateur))
                .toList();
    }

    private Utilisateur requireCurrentUtilisateur() {
        Long utilisateurId = AuthContext.requireUserId();

        return utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new UnauthorizedException("Utilisateur introuvable"));
    }

    private CatalogueDto.CatalogueItemResponse toCatalogueItemResponse(
            Livre livre,
            Utilisateur utilisateurCourant
    ) {
        int quantiteDisponible = livreService.calculateDisponible(livre);

        boolean emprunteParUtilisateur = false;

        if (utilisateurCourant.getRole() == Role.ADHERENT) {
            emprunteParUtilisateur = empruntRepository
                    .existsByUtilisateurIdAndLivreIdAndStatut(
                            utilisateurCourant.getId(),
                            livre.getId(),
                            StatutEmprunt.EN_COURS
                    );
        }

        return new CatalogueDto.CatalogueItemResponse(
                livre.getId(),
                livre.getTitre(),
                livre.getAuteur(),
                livre.getDescription(),
                toPublicImageUrl(livre.getImageUrl()),
                livre.getQuantiteTotale(),
                quantiteDisponible,
                emprunteParUtilisateur
        );
    }

    private String toPublicImageUrl(String storedFilename) {
        if (storedFilename == null || storedFilename.isBlank()) {
            return null;
        }
        return "/api/media/livres/" + storedFilename;
    }
}
