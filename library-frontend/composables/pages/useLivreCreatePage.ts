import { ref } from "vue";
import { useToast } from "~/composables/ui/useToast";
import { createLivre } from "~/services/livresService";
import { Langue } from "~/types/shared/langue";
import type { ApiThrownError } from "~/services/core/httpErrors";
import { extractHttpErrorMessage } from "~/utils/errors/apiErrors";
import {
  emptyLivreFormValue,
  type LivreFormValue,
} from "~/types/livres";
import { formValueToCreateLivreRequest } from "~/utils/mappers/livreFormMapper";

function isValidLangue(value: string): value is Langue {
  return value === Langue.FR || value === Langue.EN || value === Langue.ES;
}

export function useLivreCreatePage() {
  const { push } = useToast();

  const saving = ref(false);
  const errorMessage = ref("");

  const initialForm = ref<LivreFormValue>({ ...emptyLivreFormValue });

  async function submit(value: LivreFormValue) {
    // Garde-fou client : la langue est @NotNull cote backend, on rejette
    // localement avant meme d'aller chercher un 400 reseau.
    if (!isValidLangue(value.langue)) {
      errorMessage.value = "Veuillez sélectionner une langue (FR, EN ou ES)";
      push("error", errorMessage.value);
      return;
    }

    saving.value = true;
    errorMessage.value = "";

    try {
      const created = await createLivre(formValueToCreateLivreRequest(value));

      push("success", "Livre créé avec succès");
      await navigateTo(`/livres/${created.id}`);
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      errorMessage.value = extractHttpErrorMessage(
        error,
        "Impossible de créer le livre",
      );
      push("error", errorMessage.value);
    } finally {
      saving.value = false;
    }
  }

  return {
    saving,
    errorMessage,
    initialForm,
    submit,
  };
}
