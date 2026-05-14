import { computed, ref } from "vue";
import { useToast } from "~/composables/ui/useToast";
import { getLivreById, updateLivre } from "~/services/livresService";
import type { LivreResponse, LivreFormValue } from "~/types/livres";
import { emptyLivreFormValue } from "~/types/livres";
import { Langue } from "~/types/shared/langue";
import type { ApiThrownError } from "~/services/core/httpErrors";
import { extractHttpErrorMessage } from "~/utils/errors/apiErrors";
import {
  livreToFormValue,
  formValueToUpdateLivreRequest,
} from "~/utils/mappers/livreFormMapper";

function isValidLangue(value: string): value is Langue {
  return value === Langue.FR || value === Langue.EN || value === Langue.ES;
}

export function useLivreEditPage() {
  const { push } = useToast();

  const livre = ref<LivreResponse | null>(null);
  const loading = ref(false);
  const saving = ref(false);
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

  const initialForm = computed<LivreFormValue>(() => {
    if (!livre.value) {
      return { ...emptyLivreFormValue };
    }
    return livreToFormValue(livre.value);
  });

  async function submit(id: number, value: LivreFormValue) {
    if (!isValidLangue(value.langue)) {
      errorMessage.value = "Veuillez sélectionner une langue (FR, EN ou ES)";
      push("error", errorMessage.value);
      return;
    }

    saving.value = true;
    errorMessage.value = "";

    try {
      const updated = await updateLivre(
        id,
        formValueToUpdateLivreRequest(value),
      );

      livre.value = updated;
      push("success", "Livre mis à jour avec succès");
      await navigateTo(`/livres/${id}`);
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      errorMessage.value = extractHttpErrorMessage(
        error,
        "Impossible de mettre à jour le livre",
        { 404: "Livre introuvable" },
      );
      push("error", errorMessage.value);
    } finally {
      saving.value = false;
    }
  }

  return {
    livre,
    loading,
    saving,
    errorMessage,
    initialForm,
    fetchLivre,
    submit,
  };
}
