import { http } from "~/services/core/http";
import type { UploadImageResponse } from "~/types/uploads";

/**
 * Upload de fichier (multipart/form-data).
 *
 * Le wrapper http() detecte que le body est un FormData et NE fixe PAS
 * Content-Type : le navigateur l'ajoute lui-meme avec le bon boundary
 * (ex: multipart/form-data; boundary=----WebKitFormBoundaryXxx).
 */

export async function uploadLivreImage(file: File): Promise<UploadImageResponse> {
  const formData = new FormData();
  formData.append("file", file);

  return await http<UploadImageResponse>("/api/livres/images/upload", {
    method: "POST",
    body: formData,
    auth: true,
  });
}
