package com.chague.bibliotheque.infrastructure.persistence;

import com.chague.bibliotheque.domain.Role;
import com.chague.bibliotheque.domain.StatutUtilisateur;
import com.chague.bibliotheque.domain.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByRole(Role role);

    List<Utilisateur> findByRole(Role role);

    List<Utilisateur> findByStatut(StatutUtilisateur statut);

    List<Utilisateur> findByRoleAndStatut(Role role, StatutUtilisateur statut);
}
