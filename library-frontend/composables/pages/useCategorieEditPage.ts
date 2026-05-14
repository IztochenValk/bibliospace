import { computed, ref } from "vue";
import { useToast } from "~/composables/ui/useToast";
import { getCategorieById, updateCategorie } from "~/services/categoriesService";
import type { Categorie } from "~/types/categories";
import type { ApiThrownError } from "~/services/core/httpErrors";
import { extractHttpErrorMessage } from "~/utils/errors/apiErrors";

/**
 * Composable de la page /categories/{id}/edit.
 *
 * Même pattern que useLivreEditPage : on charge la ressource cible à
 * l'arrivée sur la page, on construit un initialForm dérivé, et on
 * délègue la soumission au service. Toutes les erreurs HTTP passent par
 * extractHttpErrorMessage avec un override 404 propre au domaine.
 */
type CategorieFormValue = {
  nomCategorie: string;
};

export function useCategorieEditPage() {
  const { push } = useToast();

  const categorie = ref<Categorie | null>(null);
  const loading = ref(false);
  const saving = ref(false);
  const errorMessage = ref("");

  const overrides = {
    404: "Catégorie introuvable",
    409: "Une catégorie avec ce nom existe déjà",
  } as const;

  async function fetchCategorie(id: number) {
    loading.value = true;
    errorMessage.value = "";

    try {
      categorie.value = await getCategorieById(id);
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      errorMessage.value = extractHttpErrorMessage(
        error,
        "Erreur lors du chargement de la catégorie",
        overrides,
      );
      categorie.value = null;
    } finally {
      loading.value = false;
    }
  }

  const initialForm = computed<CategorieFormValue>(() => {
    if (!categorie.value) {
      return { nomCategorie: "" };
    }
    return { nomCategorie: categorie.value.nomCategorie };
  });

  async function submit(id: number, value: CategorieFormValue) {
    saving.value = true;
    errorMessage.value = "";

    try {
      const updated = await updateCategorie(id, {
        nomCategorie: value.nomCategorie.trim(),
      });
      categorie.value = updated;
      push("success", "Catégorie mise à jour avec succès");
      await navigateTo("/categories");
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      errorMessage.value = extractHttpErrorMessage(
        error,
        "Impossible de mettre à jour la catégorie",
        overrides,
      );
      push("error", errorMessage.value);
    } finally {
      saving.value = false;
    }
  }

  return {
    categorie,
    loading,
    saving,
    errorMessage,
    initialForm,
    fetchCategorie,
    submit,
  };
}
