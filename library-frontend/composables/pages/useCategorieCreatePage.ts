import { ref } from "vue";
import { useToast } from "~/composables/ui/useToast";
import { createCategorie } from "~/services/categoriesService";
import type { ApiThrownError } from "~/services/core/httpErrors";
import { extractHttpErrorMessage } from "~/utils/errors/apiErrors";

type CategorieFormValue = {
  nomCategorie: string;
};

export function useCategorieCreatePage() {
  const { push } = useToast();

  const saving = ref(false);
  const errorMessage = ref("");

  const initialForm = ref<CategorieFormValue>({
    nomCategorie: "",
  });

  async function submit(value: CategorieFormValue) {
    saving.value = true;
    errorMessage.value = "";

    try {
      await createCategorie({
        nomCategorie: value.nomCategorie.trim(),
      });

      push("success", "Catégorie créée avec succès");
      await navigateTo("/categories");
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      errorMessage.value = extractHttpErrorMessage(
        error,
        "Impossible de créer la catégorie",
        { 409: "Une catégorie avec ce nom existe déjà" },
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
