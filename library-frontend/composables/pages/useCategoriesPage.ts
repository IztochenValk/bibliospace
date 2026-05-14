import { computed, ref } from "vue";
import { useToast } from "~/composables/ui/useToast";
import { deleteCategorie, listCategories } from "~/services/categoriesService";
import type { Categorie } from "~/types/categories";
import type { ApiThrownError } from "~/services/core/httpErrors";
import { extractHttpErrorMessage } from "~/utils/errors/apiErrors";

/**
 * Composable de la page /categories.
 *
 * En miroir de useLivresPage : liste paginée + flux de confirmation avant
 * suppression. On bloque côté UI la suppression d'une catégorie déjà liée
 * à un livre (champ `utilisee` côté DTO) pour éviter un aller-retour 409
 * Conflict — la règle est aussi vérifiée côté back par CategorieService.
 */
export function useCategoriesPage() {
  const { push } = useToast();

  const items = ref<Categorie[]>([]);
  const loading = ref(false);
  const actionLoadingId = ref<number | null>(null);
  const errorMessage = ref("");
  const pendingDelete = ref<Categorie | null>(null);

  const overrides = {
    404: "Catégorie introuvable",
    409: "Cette catégorie est utilisée par au moins un livre",
  } as const;

  async function fetchCategories() {
    loading.value = true;
    errorMessage.value = "";

    try {
      items.value = await listCategories();
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      items.value = [];
      errorMessage.value = extractHttpErrorMessage(
        error,
        "Erreur lors du chargement des catégories",
        overrides,
      );
    } finally {
      loading.value = false;
    }
  }

  async function remove(categorie: Categorie) {
    actionLoadingId.value = categorie.id;

    try {
      await deleteCategorie(categorie.id);
      push("success", "Catégorie supprimée avec succès");
      await fetchCategories();
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      push(
        "error",
        extractHttpErrorMessage(
          error,
          "Impossible de supprimer cette catégorie",
          overrides,
        ),
      );
    } finally {
      actionLoadingId.value = null;
    }
  }

  // ==============================
  // Flux de confirmation
  // ==============================

  const confirmTitle = computed(() => "Supprimer cette catégorie ?");

  const confirmMessage = computed(() => {
    const cat = pendingDelete.value;
    if (!cat) return "";
    return `« ${cat.nomCategorie} » sera définitivement supprimée.\nCette action est irréversible.`;
  });

  const isConfirming = computed(() => actionLoadingId.value !== null);

  function askRemove(categorie: Categorie) {
    pendingDelete.value = categorie;
  }

  function cancelPendingDelete() {
    pendingDelete.value = null;
  }

  async function confirmPendingDelete() {
    const cat = pendingDelete.value;
    if (!cat) return;
    await remove(cat);
    pendingDelete.value = null;
  }

  return {
    items,
    loading,
    actionLoadingId,
    errorMessage,
    fetchCategories,
    remove,

    pendingDelete,
    confirmTitle,
    confirmMessage,
    isConfirming,
    askRemove,
    confirmPendingDelete,
    cancelPendingDelete,
  };
}
