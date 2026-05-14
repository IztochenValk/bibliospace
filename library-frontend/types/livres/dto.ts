import type { CategorieSummaryResponse } from "~/types/categories/dto";
import type { Langue } from "~/types/shared/langue";

/**
 * Miroir TypeScript des DTOs Java de LivreDto.java.
 *
 * Alignement strict avec les contraintes Jakarta du backend :
 *   - titre, auteur     : @NotBlank  -> string non optionnel
 *   - langue            : @NotNull   -> Langue (FR | EN | ES) obligatoire
 *   - description, isbn : optionnels -> nullable
 *   - quantiteTotale    : @Min(0)    -> nombre positif ou null (default 0 cote service)
 *   - categorieIds      : optionnel  -> peut etre omis ou liste vide
 */

export type CreateLivreRequest = {
  titre: string;
  auteur: string;
  description?: string | null;
  langue: Langue;
  imageUrl?: string | null;
  isbn?: string | null;
  quantiteTotale?: number | null;
  categorieIds?: number[] | null;
};

export type UpdateLivreRequest = {
  titre: string;
  auteur: string;
  description?: string | null;
  langue: Langue;
  imageUrl?: string | null;
  isbn?: string | null;
  quantiteTotale?: number | null;
  categorieIds?: number[] | null;
};

export type LivreResponse = {
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

/**
 * Miroir EXACT de LivreSummaryResponse cote backend : id, titre, auteur.
 * Le backend ne renvoie PAS imageUrl dans ce DTO leger.
 */
export type LivreSummaryResponse = {
  id: number;
  titre: string;
  auteur: string;
};
