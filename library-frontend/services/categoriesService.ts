import { http } from "~/services/core/http";
import type {
  CreateCategorieRequest,
  UpdateCategorieRequest,
  CategorieResponse,
} from "~/types/categories/dto";
import type { Categorie } from "~/types/categories/model";

/**
 * Couche services du domaine "categories".
 * Voir architecture en deux couches dans services/livresService.ts.
 */

function toCategorie(dto: CategorieResponse): Categorie {
  return { ...dto };
}

export async function createCategorie(payload: CreateCategorieRequest): Promise<Categorie> {
  const dto = await http<CategorieResponse>("/api/categories", {
    method: "POST",
    body: payload,
    auth: true,
  });
  return toCategorie(dto);
}

export async function listCategories(): Promise<Categorie[]> {
  const dtos = await http<CategorieResponse[]>("/api/categories", {
    method: "GET",
    auth: true,
  });
  return dtos.map(toCategorie);
}

export async function getCategorieById(id: number): Promise<Categorie> {
  const dto = await http<CategorieResponse>(`/api/categories/${id}`, {
    method: "GET",
    auth: true,
  });
  return toCategorie(dto);
}

export async function updateCategorie(
  id: number,
  payload: UpdateCategorieRequest,
): Promise<Categorie> {
  const dto = await http<CategorieResponse>(`/api/categories/${id}`, {
    method: "PUT",
    body: payload,
    auth: true,
  });
  return toCategorie(dto);
}

export async function deleteCategorie(id: number): Promise<void> {
  await http<null>(`/api/categories/${id}`, {
    method: "DELETE",
    auth: true,
  });
}
