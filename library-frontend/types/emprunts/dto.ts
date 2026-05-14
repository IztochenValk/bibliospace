import type { StatutEmprunt } from "~/types/shared";

/**
 * Miroir TypeScript des DTOs Java de EmpruntDto.java. On respecte le parallélisme des formes.
 *
 * Côté requête :
 *   - livreId         : @NotNull
 *   - dateRetourPrevue: @NotNull, @FutureOrPresent (format ISO date "YYYY-MM-DD")
 *
 * Côté réponse :
 *   - dateRetourEffective est null tant que le livre n'a pas été rendu.
 *   - imageUrl est null si le livre n'a pas de couverture associée.
 *   - utilisateurId/nomUtilisateur/prenomUtilisateur et livreId/titreLivre
 *     sont nullables par défense en profondeur côté mapper backend
 *     (pattern utilisateur != null ? ... : null), mais en exploitation
 *     normale ils ne sont JAMAIS null : les utilisateurs ne sont pas
 *     supprimés physiquement (anonymisation = substitution par
 *     "ANONYMISE" / "UTILISATEUR-{id}"), et un livre encore lié à un
 *     emprunt ne peut pas être supprimé (DeleteLivreImpossibleException).
 */

export type CreateEmpruntRequest = {
  livreId: number;
  dateRetourPrevue: string;
};

export type RetourEmpruntRequest = {
  dateRetourEffective?: string | null;
};

export type EmpruntResponse = {
  id: number;
  utilisateurId: number | null;
  nomUtilisateur: string | null;
  prenomUtilisateur: string | null;
  livreId: number | null;
  titreLivre: string | null;
  imageUrl: string | null;
  dateEmprunt: string;
  dateRetourPrevue: string;
  dateRetourEffective: string | null;
  statut: StatutEmprunt;
};
