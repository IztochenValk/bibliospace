import { http } from "~/services/core/http";
import type { CatalogueItemResponse } from "~/types/catalogue/dto";
import type { CatalogueItem } from "~/types/catalogue/model";

/**
 * Couche services du domaine "catalogue" (vue agrégée des livres exposée
 * aux adhérents et au public). Voir architecture en deux couches dans
 * services/livresService.ts.
 *
 * Les permissions d'affichage et d'action (canBorrow, etc.) restent dans
 * le module dédié services/catalogue/cataloguePermissions.ts car ce sont
 * des règles de présentation, pas du transport HTTP.
 */

function toCatalogueItem(dto: CatalogueItemResponse): CatalogueItem {
  return { ...dto };
}

export async function listCatalogue(): Promise<CatalogueItem[]> {
  const dtos = await http<CatalogueItemResponse[]>("/api/catalogue", {
    method: "GET",
    auth: true,
  });
  return dtos.map(toCatalogueItem);
}
