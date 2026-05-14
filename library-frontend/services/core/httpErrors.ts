import type { ApiError } from "~/types/shared";

export type ApiThrownError = Error & {
  api?: ApiError;
  status?: number;
};

export function toApiThrownError(err: unknown): ApiThrownError {
  const error = err as {
    data?: ApiError;
    status?: number;
    message?: string;
  };

  const thrown = new Error(error.message || "la requête HTTP a échoué") as ApiThrownError;
  thrown.api = error.data;
  thrown.status = error.status;
  return thrown;
}
