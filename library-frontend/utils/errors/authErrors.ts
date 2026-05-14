import type { ApiThrownError } from "~/services/core/httpErrors";
import { extractHttpErrorMessage } from "~/utils/errors/apiErrors";

export function extractLoginErrorMessage(error: ApiThrownError): string {
  return extractHttpErrorMessage(error, "Échec de la connexion", {
    401: "Identifiants invalides",
  });
}
