package com.chague.bibliotheque.infrastructure.config;

import com.chague.bibliotheque.domain.Role;
import com.chague.bibliotheque.domain.StatutUtilisateur;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeedUserItem {
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private Role role;
    private StatutUtilisateur statut;
}
