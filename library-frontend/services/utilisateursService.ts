import { http } from "~/services/core/http";
import type {
  UpdateUtilisateurRequest,
  UtilisateurResponse,
} from "~/types/utilisateurs/dto";
import type { Utilisateur } from "~/types/utilisateurs/model";
import type { Role, StatutUtilisateur } from "~/types/shared";

/**
 * Couche services du domaine "utilisateurs".
 * Voir architecture en deux couches dans services/livresService.ts.
 *
 * Les regles de permission UI (qui peut editer/desactiver/anonymiser qui)
 * sont dans services/utilisateursPolicies.ts.
 */

function toUtilisateur(dto: UtilisateurResponse): Utilisateur {
  return { ...dto };
}

export async function listUtilisateurs(): Promise<Utilisateur[]> {
  const dtos = await http<UtilisateurResponse[]>("/api/utilisateurs", {
    method: "GET",
    auth: true,
  });
  return dtos.map(toUtilisateur);
}

export async function getUtilisateurById(id: number): Promise<Utilisateur> {
  const dto = await http<UtilisateurResponse>(`/api/utilisateurs/${id}`, {
    method: "GET",
    auth: true,
  });
  return toUtilisateur(dto);
}

export async function updateUtilisateur(id: number, payload: UpdateUtilisateurRequest): Promise<Utilisateur> {
  const dto = await http<UtilisateurResponse>(`/api/utilisateurs/${id}`, {
    method: "PUT",
    body: payload,
    auth: true,
  });
  return toUtilisateur(dto);
}

export async function listUtilisateursByRole(role: Role): Promise<Utilisateur[]> {
  const dtos = await http<UtilisateurResponse[]>(`/api/utilisateurs/role/${role}`, {
    method: "GET",
    auth: true,
  });
  return dtos.map(toUtilisateur);
}

export async function listUtilisateursByStatut(statut: StatutUtilisateur): Promise<Utilisateur[]> {
  const dtos = await http<UtilisateurResponse[]>(`/api/utilisateurs/statut/${statut}`, {
    method: "GET",
    auth: true,
  });
  return dtos.map(toUtilisateur);
}

export async function deactivateUtilisateur(id: number): Promise<void> {
  await http<null>(`/api/utilisateurs/${id}/deactivate`, {
    method: "PATCH",
    auth: true,
  });
}

export async function reactivateUtilisateur(id: number): Promise<void> {
  await http<null>(`/api/utilisateurs/${id}/reactivate`, {
    method: "PATCH",
    auth: true,
  });
}

export async function anonymizeUtilisateur(id: number): Promise<void> {
  await http<null>(`/api/utilisateurs/${id}/anonymize`, {
    method: "PATCH",
    auth: true,
  });
}
