import { ref } from "vue";
import { getLivreById } from "~/services/livresService";
import type { LivreResponse } from "~/types/livres";
import type { ApiThrownError } from "~/services/core/httpErrors";
import { extractHttpErrorMessage } from "~/utils/errors/apiErrors";

export function useLivreDetailPage() {
  const livre = ref<LivreResponse | null>(null);
  const loading = ref(false);
  const errorMessage = ref("");

  async function fetchLivre(id: number) {
    loading.value = true;
    errorMessage.value = "";

    try {
      livre.value = await getLivreById(id);
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      errorMessage.value = extractHttpErrorMessage(
        error,
        "Erreur lors du chargement du livre",
        { 404: "Livre introuvable" },
      );
      livre.value = null;
    } finally {
      loading.value = false;
    }
  }

  return {
    livre,
    loading,
    errorMessage,
    fetchLivre,
  };
}
