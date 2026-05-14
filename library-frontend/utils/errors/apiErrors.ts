import type { ApiThrownError } from "~/services/core/httpErrors";

/**
 * Extraction "minimale" du message d'erreur HTTP : on regarde uniquement
 * le payload renvoyé par le back (api.message > api.error), à défaut le
 * message brut de l'Error JS, à défaut le fallback fourni.
 *
 * Utilisé en interne par {@link extractHttpErrorMessage} et exporté pour
 * les cas où l'on n'a aucune logique conditionnelle par status HTTP à
 * appliquer (ex. action déjà confirmée côté client).
 */
export function extractDefaultApiErrorMessage(
  error: ApiThrownError,
  fallback: string,
): string {
  return (
    error.api?.message ||
    error.api?.error ||
    error.message ||
    fallback
  );
}

/**
 * Extraction "complète" du message d'erreur HTTP, avec :
 *
 *   1. Priorité absolue aux fieldErrors si présents (validation Jakarta côté
 *      back qui remonte sur les @NotBlank / @Pattern / @Size, etc.).
 *      On les concatène avec " | " pour rester compact côté UI.
 *
 *   2. Override explicite par status HTTP fourni par l'appelant
 *      (ex. {404: "Livre introuvable"}). Permet à chaque page de garder sa
 *      formulation métier sans dupliquer toute la cascade de status.
 *
 *   3. Messages par défaut pour les status HTTP transverses qui ne dépendent
 *      pas du domaine (0 = réseau, 400 = données invalides, 401 = session
 *      invalide, 403 = accès refusé).
 *
 *   4. Cascade api.message > api.error > error.message > fallback applicatif.
 *
 * @param error      l'erreur typée renvoyée par le wrapper http()
 * @param fallback   message générique de dernier recours (ex. "Impossible de
 *                   charger les livres") — propre à l'action en cours
 * @param overrides  table optionnelle status → message pour les cas métiers
 *                   où la formulation par défaut n'est pas assez précise
 */
export function extractHttpErrorMessage(
  error: ApiThrownError,
  fallback: string,
  overrides?: Partial<Record<number, string>>,
): string {
  // 1. fieldErrors prennent priorité même sur un override 400, car ils
  //    apportent plus d'information (champ par champ) qu'un message générique.
  const fieldErrors = error.api?.fieldErrors;
  if (fieldErrors && Object.keys(fieldErrors).length > 0) {
    return Object.values(fieldErrors).join(" | ");
  }

  const status = error.status;

  // 2. Override par page (404 → "Livre introuvable", etc.)
  if (status !== undefined && overrides?.[status]) {
    return overrides[status]!;
  }

  // 3. Messages par défaut transverses
  switch (status) {
    case 0:
      return "Impossible de joindre le serveur";
    case 400:
      return "Données invalides";
    case 401:
      return "Session invalide";
    case 403:
      return "Accès refusé";
  }

  // 4. Cascade payload → message JS → fallback
  return extractDefaultApiErrorMessage(error, fallback);
}
