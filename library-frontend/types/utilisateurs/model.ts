import type { Role, StatutUtilisateur } from "~/types/shared";

export type Utilisateur = {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  role: Role;
  statut: StatutUtilisateur;
};

export type UtilisateurSummary = {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  role: Role;
  statut: StatutUtilisateur;
};
