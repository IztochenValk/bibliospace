import type { DecodedJwtClaims } from "~/types/auth/dto";

/**
 * Decode la partie "payload" d'un JWT en base64url.
 *
 * ATTENTION : cette fonction ne verifie PAS la signature du token.
 * Elle sert uniquement a lire les claims cote client pour peupler
 * le state (uid, roles, exp). La validation cryptographique est
 * effectuee par le backend Spring Security a chaque requete.
 */
export function decodeJwt(token: string): DecodedJwtClaims | null {
  if (!token || typeof token !== "string") {
    return null;
  }

  const parts = token.split(".");
  if (parts.length !== 3) {
    return null;
  }

  try {
    const payload = parts[1];
    const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
    const padded = normalized + "=".repeat((4 - (normalized.length % 4)) % 4);

    const decoded = typeof window !== "undefined"
      ? atob(padded)
      : Buffer.from(padded, "base64").toString("utf-8");

    // Les caracteres multi-octets doivent etre decodes en UTF-8
    const utf8 = decodeURIComponent(
      decoded
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );

    return JSON.parse(utf8) as DecodedJwtClaims;
  } catch {
    return null;
  }
}

/**
 * Convertit la claim "exp" (epoch seconds) en timestamp millisecondes
 * pour comparaison directe avec Date.now().
 */
export function jwtExpMs(claims: DecodedJwtClaims | null): number | null {
  if (!claims || typeof claims.exp !== "number") {
    return null;
  }
  return claims.exp * 1000;
}

/**
 * Indique si le JWT est expire par rapport a l'horloge locale.
 * Renvoie false si l'expiration est inconnue (pas de blocage abusif).
 */
export function isJwtExpired(token: string | null | undefined): boolean {
  if (!token) {
    return true;
  }
  const claims = decodeJwt(token);
  const expMs = jwtExpMs(claims);
  if (!expMs) {
    return false;
  }
  return Date.now() >= expMs;
}
