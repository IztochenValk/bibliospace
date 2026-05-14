/**
 * Réponse de POST /api/livres/images/upload.
 *
 * Miroir TypeScript du record Java
 * com.chague.bibliotheque.api.dto.ImageUploadDto.UploadImageResponse :
 *   - fileName    : nom UUID.ext attribué au fichier stocké côté serveur
 *   - imageUrl    : URL relative servie par /api/media/livres/{fileName}
 *   - size        : taille du fichier en octets
 *   - contentType : MIME type détecté par Tika (image/jpeg, image/png, ...)
 *
 * Côté formulaire, seul `imageUrl` est consommé (voir useImageUpload).
 */
export type UploadImageResponse = {
  fileName: string;
  imageUrl: string;
  size: number;
  contentType: string;
};
