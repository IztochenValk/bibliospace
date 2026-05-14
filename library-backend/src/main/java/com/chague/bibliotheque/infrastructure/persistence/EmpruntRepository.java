package com.chague.bibliotheque.infrastructure.persistence;

import com.chague.bibliotheque.domain.Emprunt;
import com.chague.bibliotheque.domain.StatutEmprunt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmpruntRepository extends JpaRepository<Emprunt, Long> {

    List<Emprunt> findByUtilisateurId(Long utilisateurId);

    List<Emprunt> findByUtilisateurIdAndStatut(Long utilisateurId, StatutEmprunt statut);

    List<Emprunt> findByStatut(StatutEmprunt statut);
    
    long countByLivreIdAndStatut(Long livreId, StatutEmprunt statut);

    boolean existsByUtilisateurIdAndLivreIdAndStatut(
            Long utilisateurId,
            Long livreId,
            StatutEmprunt statut
    );

    boolean existsByUtilisateurIdAndStatut(Long utilisateurId, StatutEmprunt statut);

    boolean existsByLivreId(Long livreId);
    
    @Query("""
            SELECT e FROM Emprunt e
            JOIN e.utilisateur u
            JOIN e.livre l
            WHERE (LOWER(u.nom)    LIKE LOWER(CONCAT('%', :nom, '%'))
                OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :nom, '%')))
              AND LOWER(l.titre)   LIKE LOWER(CONCAT('%', :titre, '%'))
            ORDER BY e.dateEmprunt DESC
            """)
    List<Emprunt> rechercherParNomEtTitre(
            @Param("nom") String nom,
            @Param("titre") String titre
    );
}
