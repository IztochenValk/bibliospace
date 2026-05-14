import { computed, ref, type Ref } from "vue";
import { uploadLivreImage } from "~/services/uploadsService";

/**
 * Composable d'upload d'image (multipart) pour les formulaires.
 *
 * Encapsule :
 *  - la sélection de fichier (input[type=file])
 *  - l'appel d'upload via uploadsService
 *  - l'état de chargement et le message d'erreur
 *  - la résolution de l'URL pour l'affichage (relative vers apiBase / absolue)
 *
 * Le composable ne possède PAS l'imageUrl du formulaire : il mute le ref
 * passé en argument. C'est volontaire : ainsi le parent garde la maîtrise
 * de son état et peut réagir aux changements (validation, dirty flag, etc.).
 *
 * Usage typique (dans un <script setup>) :
 *
 *   import { toRef } from "vue";
 *   import { useImageUpload } from "~/composables/forms/useImageUpload";
 *
 *   const localValue = reactive({ imageUrl: "" });
 *   const imageUrlRef = toRef(localValue, "imageUrl");
 *
 *   const {
 *     fileInputRef, selectedFile, uploadLoading, uploadErrorMessage,
 *     resolvedPreviewUrl,
 *     onFileChange, uploadImage, clearImage,
 *   } = useImageUpload(imageUrlRef);
 */
export function useImageUpload(imageUrl: Ref<string>) {
  const config = useRuntimeConfig();

  const fileInputRef = ref<HTMLInputElement | null>(null);
  const selectedFile = ref<File | null>(null);
  const uploadLoading = ref(false);
  const uploadErrorMessage = ref("");

  /** Aperçu : valeur "telle quelle" si une image est définie, sinon "". */
  const previewImageUrl = computed(() => imageUrl.value.trim());

  /**
   * Aperçu résolu : si l'URL est absolue (http/https) on la prend telle
   * quelle, sinon on la préfixe par config.public.apiBase pour transformer
   * un chemin relatif type "/api/media/livres/xxx.png" en URL exploitable
   * par <img src>.
   */
  const resolvedPreviewUrl = computed(() => {
    const value = previewImageUrl.value;
    if (!value) return "";

    if (value.startsWith("http://") || value.startsWith("https://")) {
      return value;
    }

    const base = String(config.public.apiBase || "").replace(/\/$/, "");
    return `${base}${value.startsWith("/") ? value : `/${value}`}`;
  });

  function onFileChange(event: Event) {
    uploadErrorMessage.value = "";
    const input = event.target as HTMLInputElement;
    selectedFile.value = input.files?.[0] ?? null;
  }

  async function uploadImage() {
    if (!selectedFile.value) {
      uploadErrorMessage.value = "Aucun fichier sélectionné";
      return;
    }

    uploadLoading.value = true;
    uploadErrorMessage.value = "";

    try {
      const response = await uploadLivreImage(selectedFile.value);
      // Mutation du ref parent : c'est le formulaire qui garde l'autorité.
      imageUrl.value = response.imageUrl;
      selectedFile.value = null;
      if (fileInputRef.value) {
        fileInputRef.value.value = "";
      }
    } catch (err: any) {
      uploadErrorMessage.value =
        err?.api?.message ||
        err?.api?.error ||
        err?.message ||
        "Impossible d'uploader l'image";
    } finally {
      uploadLoading.value = false;
    }
  }

  function clearImage() {
    imageUrl.value = "";
    selectedFile.value = null;
    uploadErrorMessage.value = "";
    if (fileInputRef.value) {
      fileInputRef.value.value = "";
    }
  }

  return {
    fileInputRef,
    selectedFile,
    uploadLoading,
    uploadErrorMessage,
    previewImageUrl,
    resolvedPreviewUrl,
    onFileChange,
    uploadImage,
    clearImage,
  };
}
