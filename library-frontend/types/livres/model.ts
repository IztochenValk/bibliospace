import type { CategorieSummaryResponse } from "~/types/categories/dto";
import type { Langue } from "~/types/shared/langue";

/**
 * Modele UI du Livre.
 *
 * Identique au DTO LivreResponse en pratique (il n'y a aucune
 * transformation profonde entre les deux), avec une nuance : le mapper
 * cote service garantit que `description` n'est jamais undefined cote
 * UI (chaine vide a la place de null si besoin), mais on garde le |null
 * dans le type pour rester aligne avec le contrat serveur.
 */
export type Livre = {
  id: number;
  titre: string;
  auteur: string;
  description: string | null;
  langue: Langue;
  imageUrl: string | null;
  isbn: string | null;
  quantiteTotale: number;
  quantiteDisponible: number;
  categories: CategorieSummaryResponse[];
};

export type LivreSummary = {
  id: number;
  titre: string;
  auteur: string;
};
