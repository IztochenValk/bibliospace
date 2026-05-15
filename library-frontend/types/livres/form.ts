/**
 * Type partagé pour la représentation UI du formulaire d'un Livre.
 *
 * Ce type est volontairement **distinct** des DTOs (CreateLivreRequest,
 * UpdateLivreRequest) et du model (Livre) parce qu'il porte la
 * représentation "champ de formulaire" :
 *
 *   - les champs optionnels côté backend (description, imageUrl, isbn) sont
 *     ici typés `string` (jamais null), car v-model d'un <input> exige une
 *     valeur primitive non-null. La conversion "" ↔ null se fait dans le
 *     mapper {@link ~/utils/mappers/livreFormMapper}.
 *
 *   - `langue` reste un `string` brut (et pas un `Langue`) tant que
 *     l'utilisateur n'a rien sélectionné dans le <select>, ce qui empêche
 *     de forcer une union "FR | EN | ES" prématurément. La validation
 *     `isValidLangue` se fait au submit côté composable de page.
 *
 * Centralisé ici (au lieu d'être redéclaré dans LivreForm.vue,
 * useLivreCreatePage.ts et useLivreEditPage.ts) pour garantir qu'une
 * évolution de schéma n'oublie aucun des trois call-sites.
 */
export type LivreFormValue = {
  titre: string;
  auteur: string;
  description: string;
  langue: string;
  imageUrl: string;
  isbn: string;
  quantiteTotale: number;
  /**
   * IDs des catégories sélectionnées (multi-sélection côté UI).
   * Vide tant que l'utilisateur n'a rien coché.
   */
  categorieIds: number[];
};

/**
 * Valeur initiale neutre : utile pour le pattern useFormSync qui exige
 * un objet "default" non-null et non-undefined comme garde-fou.
 */
export const emptyLivreFormValue: LivreFormValue = {
  titre: "",
  auteur: "",
  description: "",
  langue: "",
  imageUrl: "",
  isbn: "",
  quantiteTotale: 0,
  categorieIds: [],
};
