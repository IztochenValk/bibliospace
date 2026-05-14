/**
 * Types pour la representation cote front des erreurs renvoyees par l'API.
 *
 * Le backend garantit (via ApiErrorFactory + GlobalControllerAdvice) un
 * format JSON unifie pour toute erreur 4xx ou 5xx :
 *
 *   {
 *     "timestamp": "2026-05-09T...",
 *     "status": 400,
 *     "error": "Bad Request",
 *     "message": "Validation failed",
 *     "fieldErrors": { "langue": "must not be null", ... } | null
 *   }
 *
 * Cote front, on type cet objet de facon DEFENSIVE (tous les champs en
 * optionnel + index signature) plutot que strict. Raisons :
 *
 *   1. Une reponse peut ne pas venir de notre back (502 nginx, erreur
 *      reseau, proxy intermediate) : dans ce cas, le shape ne correspond
 *      pas et un typage strict ferait crasher TypeScript.
 *
 *   2. Le wrapper http() ne valide pas le payload : il l'expose tel quel
 *      via .api. Mieux vaut laisser le consommateur faire la lecture
 *      defensive (err?.api?.message) que de promettre un contrat strict
 *      qui peut etre viole en runtime.
 *
 * Pour la generation cote back, voir ApiErrorFactory.java.
 */

export type ApiFieldErrors = Record<string, string>;

export type ApiError = {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  fieldErrors?: ApiFieldErrors | null;
  /** Index signature pour tolerer un payload non standard sans casser le typage. */
  [key: string]: unknown;
};
