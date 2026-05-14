import type { StatutEmprunt } from "~/types/shared";

/**
 * Modele UI Emprunt : identique au DTO sauf que les dates sont
 * deserialisees en objets Date par le mapper du service (utile pour
 * formater/comparer cote composants sans repasser par new Date()).
 */
export type Emprunt = {
  id: number;
  utilisateurId: number | null;
  nomUtilisateur: string | null;
  prenomUtilisateur: string | null;
  livreId: number | null;
  titreLivre: string | null;
  imageUrl: string | null;

  dateEmprunt: Date | null;
  dateRetourPrevue: Date | null;
  dateRetourEffective: Date | null;

  statut: StatutEmprunt;
};
