import { http } from "~/services/core/http";
import type { UtilisateurResponse } from "~/types/utilisateurs/dto";

/**
 * Endpoints "self-service" : actions de l'utilisateur courant sur sa
 * propre fiche (lecture, mise a jour, changement de mot de passe).
 */

export type UpdateMyProfileRequest = {
  nom: string;
  prenom: string;
  email: string;
};

export type UpdateMyPasswordRequest = {
  ancienMotDePasse: string;
  nouveauMotDePasse: string;
};

export function getMyProfile() {
  return http<UtilisateurResponse>("/api/utilisateurs/me", {
    method: "GET",
    auth: true,
  });
}

export function updateMyProfile(payload: UpdateMyProfileRequest) {
  return http<UtilisateurResponse>("/api/utilisateurs/me", {
    method: "PUT",
    body: payload,
    auth: true,
  });
}

export function updateMyPassword(payload: UpdateMyPasswordRequest) {
  return http<null>("/api/utilisateurs/me/password", {
    method: "PATCH",
    body: payload,
    auth: true,
  });
}
