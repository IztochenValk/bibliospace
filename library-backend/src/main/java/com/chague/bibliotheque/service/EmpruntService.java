package com.chague.bibliotheque.service;

import com.chague.bibliotheque.api.dto.EmpruntDto;
import com.chague.bibliotheque.api.exception.emprunt.EmpruntImpossibleException;
import com.chague.bibliotheque.api.exception.emprunt.EmpruntIsNotExistsException;
import com.chague.bibliotheque.api.exception.livre.LivreIsNotExistsException;
import com.chague.bibliotheque.domain.*;
import com.chague.bibliotheque.infrastructure.persistence.EmpruntRepository;
import com.chague.bibliotheque.infrastructure.persistence.LivreRepository;
import com.chague.bibliotheque.infrastructure.persistence.UtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Service
public class EmpruntService {

    private final AuthorizationService authorizationService;
    private final EmpruntRepository empruntRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final LivreRepository livreRepository;
    private final LivreService livreService;

    @Transactional
    @PreAuthorize("hasRole('ADHERENT')")
    public EmpruntDto.EmpruntResponse create(EmpruntDto.CreateEmpruntRequest req) {

        Utilisateur utilisateur = authorizationService.requireCurrentUser();

        Livre livre = livreRepository.findById(req.livreId())
                .orElseThrow(() -> new LivreIsNotExistsException(req.livreId()));

        boolean dejaEmprunte = empruntRepository.existsByUtilisateurIdAndLivreIdAndStatut(
                utilisateur.getId(),
                livre.getId(),
                StatutEmprunt.EN_COURS
        );

        if (dejaEmprunte) {
            throw new EmpruntImpossibleException("Cet utilisateur a déjà emprunté ce livre");
        }

        if (livreService.calculateDisponible(livre) <= 0) {
            throw new EmpruntImpossibleException("Aucun exemplaire disponible pour ce livre");
        }

        Emprunt emprunt = new Emprunt();
        emprunt.setUtilisateur(utilisateur);
        emprunt.setLivre(livre);
        emprunt.setDateEmprunt(LocalDate.now());
        emprunt.setDateRetourPrevue(req.dateRetourPrevue());
        emprunt.setDateRetourEffective(null);
        emprunt.setStatut(StatutEmprunt.EN_COURS);

        Emprunt saved = empruntRepository.save(emprunt);

        return toResponse(saved);
    }

    @Transactional
    public EmpruntDto.EmpruntResponse retourner(Long id, EmpruntDto.RetourEmpruntRequest req) {
        LocalDate now = LocalDate.now();
        Emprunt emprunt = empruntRepository.findById(id)
                .orElseThrow(() -> new EmpruntIsNotExistsException(id));

        authorizationService.checkCanManageEmprunt(emprunt);

        if (emprunt.getStatut() != StatutEmprunt.EN_COURS) {
            throw new EmpruntImpossibleException("Cet emprunt n'est pas en cours");
        }

        LocalDate dateRetourEffective = req.dateRetourEffective() != null
                ? req.dateRetourEffective()
                : now;

        if (dateRetourEffective.isBefore(emprunt.getDateEmprunt())) {
            throw new EmpruntImpossibleException(
                    "La date de retour effective ne peut pas être antérieure à la date d'emprunt"
            );
        }

        if (dateRetourEffective.isAfter(now)) {
            throw new EmpruntImpossibleException(
                    "La date de retour effective ne peut pas être dans le futur"
            );
        }

        emprunt.setDateRetourEffective(dateRetourEffective);
        emprunt.setStatut(StatutEmprunt.RETOURNE);

        Emprunt saved = empruntRepository.save(emprunt);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public EmpruntDto.EmpruntResponse getById(Long id) {
        authorizationService.checkCanAccessEmprunt(id);

        Emprunt emprunt = empruntRepository.findById(id)
                .orElseThrow(() -> new EmpruntIsNotExistsException(id));

        return toResponse(emprunt);
    }

    @Transactional(readOnly = true)
    public List<EmpruntDto.EmpruntResponse> list() {
        Utilisateur acteur = authorizationService.requireCurrentUser();

        List<Emprunt> emprunts = authorizationService.isStaff(acteur)
                ? empruntRepository.findAll()
                : empruntRepository.findByUtilisateurId(acteur.getId());

        return emprunts.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmpruntDto.EmpruntResponse> listByUtilisateur(Long utilisateurId) {
        authorizationService.checkCanAccessUtilisateur(utilisateurId);

        List<Emprunt> emprunts = empruntRepository.findByUtilisateurId(utilisateurId);

        return emprunts.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmpruntDto.EmpruntResponse> listByStatut(String statut) {
        Utilisateur acteur = authorizationService.requireCurrentUser();

        StatutEmprunt statutEnum = parseStatut(statut);

        List<Emprunt> emprunts = authorizationService.isStaff(acteur)
                ? empruntRepository.findByStatut(statutEnum)
                : empruntRepository.findByUtilisateurId(acteur.getId()).stream()
                .filter(e -> e.getStatut() == statutEnum)
                .toList();

        return emprunts.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Recherche transversale d'emprunts par nom/prénom d'adhérent ET/OU par
     * titre de livre. Réservée au staff (BIBLIOTHECAIRE et ADMINISTRATEUR).
     *
     * <p>L'autorisation par rôle est déléguée à {@code @PreAuthorize}, alignée
     * sur le pattern des autres services du projet (LivreImageStorageService,
     * LivreService, CategorieService, UtilisateurService).</p>
     *
     * @param nom    fragment de nom OU prénom de l'adhérent ; chaîne vide = pas de filtre
     * @param titre  fragment de titre du livre ; chaîne vide = pas de filtre
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMINISTRATEUR')")
    public List<EmpruntDto.EmpruntResponse> rechercher(String nom, String titre) {
        String nomNormalise = nom == null ? "" : nom.trim();
        String titreNormalise = titre == null ? "" : titre.trim();

        return empruntRepository.rechercherParNomEtTitre(nomNormalise, titreNormalise)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EmpruntDto.EmpruntResponse toResponse(Emprunt emprunt) {
        Utilisateur utilisateur = emprunt.getUtilisateur();
        Livre livre = emprunt.getLivre();

        Long utilisateurId = utilisateur != null ? utilisateur.getId() : null;
        String nomUtilisateur = utilisateur != null ? utilisateur.getNom() : null;
        String prenomUtilisateur = utilisateur != null ? utilisateur.getPrenom() : null;

        Long livreId = livre != null ? livre.getId() : null;
        String titreLivre = livre != null ? livre.getTitre() : null;
        String imageUrl = livre != null ? livre.getImageUrl() : null;

        return new EmpruntDto.EmpruntResponse(
                emprunt.getId(),
                utilisateurId,
                nomUtilisateur,
                prenomUtilisateur,
                livreId,
                titreLivre,
                imageUrl,
                emprunt.getDateEmprunt(),
                emprunt.getDateRetourPrevue(),
                emprunt.getDateRetourEffective(),
                emprunt.getStatut()
        );
    }

    private StatutEmprunt parseStatut(String statut) {
        try {
            return StatutEmprunt.valueOf(statut.trim().toUpperCase());
        } catch (Exception e) {
            throw new EmpruntImpossibleException("Statut invalide : " + statut);
        }
    }
}
