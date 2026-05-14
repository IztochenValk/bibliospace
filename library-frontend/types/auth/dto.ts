/**
 * DTO d'authentification alignés sur le backend Spring.
 *
 * Le backend n'expose plus que POST /api/auth/login qui renvoie
 * un JWT signé RSA sous la forme { token: "..." }.
 */

export type LoginRequest = {
  email: string;
  password: string;
};

export type TokenResponse = {
  token: string;
};

/**
 * Payload utile décodé depuis le JWT (sans vérification de signature).
 * La source de vérité reste le backend ; ce décodage côté client sert
 * uniquement à peupler le state local (uid, roles, expiration).
 */
export type DecodedJwtClaims = {
  sub: string;
  uid: number;
  roles: string[];
  iss?: string;
  iat?: number;
  exp?: number;
};
