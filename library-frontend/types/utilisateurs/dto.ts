import type { Role, StatutUtilisateur } from "~/types/shared";

/**
 * Miroir TypeScript des DTOs Java de UtilisateurDto.java.
 *
 * Contraintes Jakarta backend :
 *   - nom, prenom : @NotBlank, @Size(max=100)
 *   - email       : @NotBlank, @Email, @Size(max=150)
 *   - motDePasse  : @NotBlank, @Size(min=8, max=255) (creation uniquement)
 *   - role        : @NotNull
 */

export type CreateUtilisateurRequest = {
  nom: string;
  prenom: string;
  email: string;
  motDePasse: string;
  role: Role;
};

export type UpdateUtilisateurRequest = {
  nom: string;
  prenom: string;
  email: string;
  role: Role;
};

export type UtilisateurResponse = {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  role: Role;
  statut: StatutUtilisateur;
};

export type UtilisateurSummaryResponse = {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  role: Role;
  statut: StatutUtilisateur;
};
