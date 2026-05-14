import { normalizeIsbn10 } from "~/utils/isbn";
import type {
  CreateLivreRequest,
  UpdateLivreRequest,
  LivreFormValue,
  Livre,
} from "~/types/livres";

/**
 * Mappers bidirectionnels entre la représentation "formulaire" et les DTOs
 * de l'API.
 *
 * Le décalage canonique entre les deux côtés :
 *
 *   - description / imageUrl / isbn / langue : formulaire = `string`
 *     (jamais null pour rester compatible v-model), backend = `string | null`
 *     en réponse et `string?` en requête. La règle est :
 *
 *       UI form -> API : "" devient null (champ optionnel non rempli).
 *       API -> UI form : null devient "" (le composant <input> a besoin
 *                        d'une primitive).
 *
 *   - isbn passe en plus par `normalizeIsbn10` au moment de l'envoi pour
 *     retirer les éventuels tirets de lisibilité et uppercaser la clé de
 *     contrôle (ex. "2-07-052-X" -> "20705X" -> "2070520X" si valide).
 *
 *   - quantiteTotale est forcé en entier positif via Math.max(0, ...) parce
 *     que l'input HTML number autorise des valeurs négatives en saisie clavier
 *     et qu'un Number(undefined) renvoie NaN.
 *
 *   - categorieIds n'est pas géré dans le formulaire actuel : on renvoie
 *     toujours `[]` côté API. La gestion des catégories se fera dans une
 *     itération future.
 *
 * Tout le code de coercion vit ici plutôt que d'être recopié dans les pages,
 * de manière à ce qu'un seul endroit ait à évoluer si le contrat change.
 */

/**
 * Hydrate un formulaire à partir d'une ressource Livre chargée depuis l'API.
 * Tous les `null` du backend sont coercés en `""` pour la liaison v-model.
 */
export function livreToFormValue(livre: Livre): LivreFormValue {
  return {
    titre: livre.titre,
    auteur: livre.auteur,
    description: livre.description ?? "",
    langue: livre.langue ?? "",
    imageUrl: livre.imageUrl ?? "",
    isbn: livre.isbn ?? "",
    quantiteTotale: livre.quantiteTotale ?? 0,
  };
}

/**
 * Construit le payload de création à partir d'une valeur de formulaire.
 *
 * La langue est attendue déjà validée par l'appelant (typage `string` côté
 * form, mais le composable de page doit vérifier `isValidLangue` avant
 * d'appeler ce mapper) — on la passe telle quelle, le backend rejettera
 * de toute façon une langue invalide via `@NotNull` sur l'enum.
 */
export function formValueToCreateLivreRequest(
  value: LivreFormValue,
): CreateLivreRequest {
  const compactIsbn = normalizeIsbn10(value.isbn);
  return {
    titre: value.titre.trim(),
    auteur: value.auteur.trim(),
    description: value.description.trim() || null,
    // cast volontaire : la page a déjà validé via isValidLangue
    langue: value.langue as CreateLivreRequest["langue"],
    imageUrl: value.imageUrl.trim() || null,
    isbn: compactIsbn || null,
    quantiteTotale: Math.max(0, Number(value.quantiteTotale) || 0),
    categorieIds: [],
  };
}

/**
 * Construit le payload de mise à jour. La structure est identique à celle
 * de création pour ce domaine, mais on garde deux fonctions distinctes
 * pour pouvoir diverger sans casse (ex. interdire la modification du
 * titre côté update plus tard).
 */
export function formValueToUpdateLivreRequest(
  value: LivreFormValue,
): UpdateLivreRequest {
  const compactIsbn = normalizeIsbn10(value.isbn);
  return {
    titre: value.titre.trim(),
    auteur: value.auteur.trim(),
    description: value.description.trim() || null,
    langue: value.langue as UpdateLivreRequest["langue"],
    imageUrl: value.imageUrl.trim() || null,
    isbn: compactIsbn || null,
    quantiteTotale: Math.max(0, Number(value.quantiteTotale) || 0),
    categorieIds: [],
  };
}
