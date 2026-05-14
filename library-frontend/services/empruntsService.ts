import { http } from "~/services/core/http";
import type {
  CreateEmpruntRequest,
  RetourEmpruntRequest,
  EmpruntResponse,
} from "~/types/emprunts/dto";
import type { Emprunt } from "~/types/emprunts/model";
import type { StatutEmprunt } from "~/types/shared";
import { toDate } from "~/utils/mappers/dateMapper";

/**
 * Couche services du domaine "emprunts".
 * Voir architecture en deux couches dans services/livresService.ts.
 */

function toEmprunt(dto: EmpruntResponse): Emprunt {
  return {
    ...dto,
    dateEmprunt: toDate(dto.dateEmprunt),
    dateRetourPrevue: toDate(dto.dateRetourPrevue),
    dateRetourEffective: toDate(dto.dateRetourEffective),
  };
}

export async function createEmprunt(payload: CreateEmpruntRequest): Promise<Emprunt> {
  const dto = await http<EmpruntResponse>("/api/emprunts", {
    method: "POST",
    body: payload,
    auth: true,
  });
  return toEmprunt(dto);
}

export async function retournerEmprunt(id: number, payload: RetourEmpruntRequest): Promise<Emprunt> {
  const dto = await http<EmpruntResponse>(`/api/emprunts/${id}/retour`, {
    method: "PATCH",
    body: payload,
    auth: true,
  });
  return toEmprunt(dto);
}

export async function getEmpruntById(id: number): Promise<Emprunt> {
  const dto = await http<EmpruntResponse>(`/api/emprunts/${id}`, {
    method: "GET",
    auth: true,
  });
  return toEmprunt(dto);
}

export async function listEmprunts(): Promise<Emprunt[]> {
  const dtos = await http<EmpruntResponse[]>("/api/emprunts", {
    method: "GET",
    auth: true,
  });
  return dtos.map(toEmprunt);
}

export async function listEmpruntsByUtilisateur(utilisateurId: number): Promise<Emprunt[]> {
  const dtos = await http<EmpruntResponse[]>(`/api/emprunts/utilisateur/${utilisateurId}`, {
    method: "GET",
    auth: true,
  });
  return dtos.map(toEmprunt);
}

export async function listEmpruntsByStatut(statut: StatutEmprunt): Promise<Emprunt[]> {
  const dtos = await http<EmpruntResponse[]>(`/api/emprunts/statut/${encodeURIComponent(statut)}`, {
    method: "GET",
    auth: true,
  });
  return dtos.map(toEmprunt);
}

export async function rechercherEmprunts(nom: string, titre: string): Promise<Emprunt[]> {
  const params = new URLSearchParams();
  if (nom) params.set("nom", nom);
  if (titre) params.set("titre", titre);

  const qs = params.toString();
  const url = qs ? `/api/emprunts/recherche?${qs}` : "/api/emprunts/recherche";

  const dtos = await http<EmpruntResponse[]>(url, {
    method: "GET",
    auth: true,
  });
  return dtos.map(toEmprunt);
}
