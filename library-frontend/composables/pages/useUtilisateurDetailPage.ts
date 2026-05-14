import { ref } from "vue";
import { getUtilisateurById } from "~/services/utilisateursService";
import type { UtilisateurResponse } from "~/types/utilisateurs";
import type { ApiThrownError } from "~/services/core/httpErrors";
import { extractHttpErrorMessage } from "~/utils/errors/apiErrors";

export function useUtilisateurDetailPage() {
  const utilisateur = ref<UtilisateurResponse | null>(null);
  const loading = ref(false);
  const errorMessage = ref("");

  async function fetchUtilisateur(id: number) {
    loading.value = true;
    errorMessage.value = "";

    try {
      utilisateur.value = await getUtilisateurById(id);
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      errorMessage.value = extractHttpErrorMessage(
        error,
        "Erreur lors du chargement de l'utilisateur",
        { 404: "Utilisateur introuvable" },
      );
      utilisateur.value = null;
    } finally {
      loading.value = false;
    }
  }

  return {
    utilisateur,
    loading,
    errorMessage,
    fetchUtilisateur,
  };
}
