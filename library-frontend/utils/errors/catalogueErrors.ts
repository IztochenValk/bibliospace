import type { ApiThrownError } from "~/services/core/httpErrors";
import { extractHttpErrorMessage } from "~/utils/errors/apiErrors";

export function extractCatalogueErrorMessage(error: ApiThrownError): string {
  return extractHttpErrorMessage(error, "Erreur lors du chargement du catalogue", {
    403: "Accès refusé au catalogue",
    404: "Catalogue introuvable",
  });
}
