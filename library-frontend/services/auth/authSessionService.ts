import type { AuthState } from "~/types";
import type { UtilisateurResponse } from "~/types/utilisateurs";
import { decodeJwt, jwtExpMs } from "~/utils/jwt";
import { Role as Roles, type Role } from "~/types/shared";

export function createEmptyAuthState(): AuthState {
  return {
    userId: null,
    nom: null,
    prenom: null,
    email: null,
    accessToken: null,
    expiresAt: null,
    role: null,
    hydrated: false,
  };
}

export function isExpiredAt(expiresAt: number | null): boolean {
  if (!expiresAt) {
    return false;
  }

  return Date.now() >= expiresAt;
}

/**
 * Convertit un role texte ("ROLE_ADMINISTRATEUR" ou "ADMINISTRATEUR")
 * en enum Role, ou null si non reconnu.
 */
function normalizeRole(raw: string | null | undefined): Role | null {
  if (!raw) {
    return null;
  }
  const stripped = raw.startsWith("ROLE_") ? raw.slice(5) : raw;
  const match = (Object.values(Roles) as string[]).find(r => r === stripped);
  return (match as Role) ?? null;
}

/**
 * Construit le state partiel a partir du JWT recu au login.
 *
 * Ce que le JWT porte nativement : uid, email (sub), roles, exp.
 * Le nom et prenom ne sont PAS dans le token ; ils seront
 * charges dans un second temps via GET /api/utilisateurs/me.
 */
export function buildAuthStateFromToken(token: string): Partial<AuthState> {
  const claims = decodeJwt(token);

  if (!claims) {
    return {
      accessToken: token,
      expiresAt: null,
    };
  }

  const primaryRole = Array.isArray(claims.roles) && claims.roles.length > 0
    ? claims.roles[0]
    : null;

  return {
    accessToken: token,
    userId: typeof claims.uid === "number" ? claims.uid : null,
    email: typeof claims.sub === "string" ? claims.sub : null,
    role: normalizeRole(primaryRole),
    expiresAt: jwtExpMs(claims),
  };
}

/**
 * Hydrate l'identite depuis la reponse GET /api/utilisateurs/me.
 * Le backend renvoie un UtilisateurResponse avec id, nom, prenom, email, role.
 */
export function buildAuthIdentityFromMe(me: UtilisateurResponse): Partial<AuthState> {
  return {
    userId: me.id,
    nom: me.nom,
    prenom: me.prenom,
    email: me.email,
    role: me.role,
  };
}

export function normalizeStoredAuthState(input: Partial<AuthState>): Partial<AuthState> {
  return {
    ...input,
    role: input.role ?? null,
    nom: input.nom ?? null,
    prenom: input.prenom ?? null,
  };
}
