import { http } from "~/services/core/http";
import type {
  CreateLivreRequest,
  UpdateLivreRequest,
  LivreResponse,
} from "~/types/livres/dto";
import type { Livre } from "~/types/livres/model";

/**
 * Couche services du domaine "livres".
 *
 * Architecture en deux couches :
 *  - couche UI    : pages + composables (state, presentation, navigation)
 *  - couche data  : ce fichier (fetch + normalisation des nullables)
 *
 * Le wrapper http() (services/core/http.ts) est une primitive partagee
 * (injection JWT, gestion uniforme des erreurs), pas une couche en soi.
 */

/**
 * Le mapper DTO -> model est un quasi no-op : le backend garantit deja
 * la coherence (langue toujours presente, categories jamais null grace a
 * la serialisation Jackson d'une List<>). On laisse le passe-plat pour
 * isoler le type model et permettre une transformation future sans
 * impact sur les consommateurs.
 */
function toLivre(dto: LivreResponse): Livre {
  return {
    ...dto,
    categories: dto.categories ?? [],
  };
}

export async function createLivre(payload: CreateLivreRequest): Promise<Livre> {
  const dto = await http<LivreResponse>("/api/livres", {
    method: "POST",
    body: payload,
    auth: true,
  });
  return toLivre(dto);
}

export async function updateLivre(id: number, payload: UpdateLivreRequest): Promise<Livre> {
  const dto = await http<LivreResponse>(`/api/livres/${id}`, {
    method: "PUT",
    body: payload,
    auth: true,
  });
  return toLivre(dto);
}

export async function getLivreById(id: number): Promise<Livre> {
  const dto = await http<LivreResponse>(`/api/livres/${id}`, {
    method: "GET",
    auth: true,
  });
  return toLivre(dto);
}

export async function listLivres(): Promise<Livre[]> {
  const dtos = await http<LivreResponse[]>("/api/livres", {
    method: "GET",
    auth: true,
  });
  return dtos.map(toLivre);
}

export async function deleteLivre(id: number): Promise<void> {
  await http<null>(`/api/livres/${id}`, {
    method: "DELETE",
    auth: true,
  });
}
