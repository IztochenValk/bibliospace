import { http } from "~/services/core/http";
import type { TokenResponse } from "~/types";

/**
 * Le backend n'expose plus qu'un seul endpoint d'authentification :
 * POST /api/auth/login. Tout le reste (profil, deconnexion) passe
 * soit par les endpoints metier (/api/utilisateurs/me), soit reste
 * purement cote client (clearAuth local pour le logout).
 *
 * Note : on passe par le wrapper http() commun (services/core/http.ts)
 * comme tous les autres *Api.ts du projet. Le login n'a pas besoin du
 * flag auth:true puisqu'il EMET le token, il ne le consomme pas.
 */
export function useAuthApi() {
  async function loginRequest(email: string, password: string): Promise<TokenResponse> {
    return await http<TokenResponse>("/api/auth/login", {
      method: "POST",
      body: { email, password },
    });
  }

  return {
    loginRequest,
  };
}
