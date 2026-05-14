import type { Role } from "~/types/shared";

/**
 * State d'authentification cote client.
 *
 * - accessToken : le JWT signe RSA recu au login.
 * - expiresAt   : timestamp (ms) d'expiration, derive de la claim "exp"
 *                 du JWT. Permet un check local rapide.
 *
 * Le refresh token n'existe plus : le backend aligne sur le formateur
 * n'expose qu'un access token. A l'expiration, l'utilisateur doit
 * se reconnecter.
 */
export type AuthState = {
  userId: number | null;
  nom: string | null;
  prenom: string | null;
  email: string | null;
  accessToken: string | null;
  expiresAt: number | null;
  role: Role | null;
  hydrated: boolean;
};
