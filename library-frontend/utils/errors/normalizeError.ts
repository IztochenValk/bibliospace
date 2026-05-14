import type { ApiThrownError } from "~/services/core/httpErrors";
import { extractDefaultApiErrorMessage } from "~/utils/errors/apiErrors";

/**
 * Catch-all pour les `catch (e: unknown)` où l'on ne sait pas si l'erreur
 * est une vraie ApiThrownError (error boundary global, logger, etc.).
 *
 * Le cast est sûr parce que extractDefaultApiErrorMessage n'accède au champs
 * qu'à travers de l'optional chaining ; une valeur qui n'est pas une
 * ApiThrownError tombe directement sur le fallback.
 */
export function normalizeError(error: unknown): string {
  return extractDefaultApiErrorMessage(
    error as ApiThrownError,
    "Une erreur inattendue est survenue",
  );
}
