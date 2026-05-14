/**
 * Utilitaires ISBN-10 — version simplifiée.
 *
 * L'ISBN est manipulé comme une chaîne compacte de 10 caractères
 * (9 chiffres + 1 clé de contrôle 0-9 ou X). L'ancienne décomposition
 * en 4 sous-champs (domaine / éditeur / publication / clé) a été retirée
 * car elle offrait peu de valeur et multipliait les points de saisie.
 */

const ISBN10_REGEX = /^\d{9}[\dX]$/;

/**
 * Génère 9 chiffres aléatoires (hors clé).
 */
function randomDigits(length: number): string {
  let out = "";
  for (let i = 0; i < length; i += 1) {
    out += Math.floor(Math.random() * 10).toString();
  }
  return out;
}

/**
 * Calcule la clé de contrôle ISBN-10 à partir des 9 premiers chiffres.
 * La somme pondérée (chiffre × (10 − position)) modulo 11 donne la clé ;
 * 10 se note "X".
 */
export function computeIsbn10CheckDigit(firstNineDigits: string): string {
  if (!/^\d{9}$/.test(firstNineDigits)) {
    throw new Error("ISBN-10 : 9 chiffres requis pour calculer la clé");
  }
  let sum = 0;
  for (let i = 0; i < 9; i += 1) {
    sum += Number(firstNineDigits[i]) * (10 - i);
  }
  const check = (11 - (sum % 11)) % 11;
  return check === 10 ? "X" : String(check);
}

/**
 * Génère un ISBN-10 valide complet (10 caractères).
 */
export function buildRandomIsbn10(): string {
  const nine = randomDigits(9);
  return nine + computeIsbn10CheckDigit(nine);
}

/**
 * Vérifie qu'une chaîne correspond à un ISBN-10 valide
 * (format + clé de contrôle correcte).
 */
export function isValidIsbn10(value: string): boolean {
  const compact = value.replace(/-/g, "").toUpperCase();
  if (!ISBN10_REGEX.test(compact)) {
    return false;
  }
  const expected = computeIsbn10CheckDigit(compact.slice(0, 9));
  return expected === compact[9];
}

/**
 * Nettoie une saisie utilisateur : retire les tirets, passe la clé en
 * majuscule, retourne une chaîne vide si la saisie était vide/null.
 */
export function normalizeIsbn10(raw: string | null | undefined): string {
  if (!raw) return "";
  return raw.replace(/-/g, "").trim().toUpperCase();
}

/**
 * Présente un ISBN-10 compact avec des tirets de lisibilité.
 * Pattern neutre "X-XX-XXXXXX-X" (1-2-6-1) ; utilisé uniquement pour
 * l'affichage, la donnée en base reste compacte.
 */
export function formatIsbn10(compact: string): string {
  const clean = normalizeIsbn10(compact);
  if (clean.length !== 10) return compact;
  return `${clean.slice(0, 1)}-${clean.slice(1, 3)}-${clean.slice(3, 9)}-${clean.slice(9, 10)}`;
}
