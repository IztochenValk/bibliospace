import { ref, computed } from "vue";
import { useToast } from "~/composables/ui/useToast";
import { listLivres, deleteLivre } from "~/services/livresService";
import type { LivreResponse } from "~/types/livres";
import type { ApiThrownError } from "~/services/core/httpErrors";
import { extractHttpErrorMessage } from "~/utils/errors/apiErrors";

export function useLivresPage() {
  const { push } = useToast();

  const loading = ref(false);
  const actionLoadingId = ref<number | null>(null);
  const items = ref<LivreResponse[]>([]);
  const pendingDelete = ref<LivreResponse | null>(null);

  const currentPage = ref(1);
  const pageSize = ref(10);
  const pageSizeOptions = [10, 15, 20];

  const totalItems = computed(() => items.value.length);

  const totalPages = computed(() => {
    return Math.max(1, Math.ceil(totalItems.value / pageSize.value));
  });

  const paginatedItems = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value;
    const end = start + pageSize.value;
    return items.value.slice(start, end);
  });

  function normalizePage() {
    if (currentPage.value > totalPages.value) {
      currentPage.value = totalPages.value;
    }

    if (currentPage.value < 1) {
      currentPage.value = 1;
    }
  }

  function goToPage(page: number) {
    currentPage.value = Math.min(Math.max(1, page), totalPages.value);
  }

  function setPageSize(size: number) {
    if (!pageSizeOptions.includes(size)) {
      return;
    }

    pageSize.value = size;
    currentPage.value = 1;
    normalizePage();
  }

  async function fetchLivres() {
    loading.value = true;

    try {
      items.value = await listLivres();
      normalizePage();
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      push("error", extractHttpErrorMessage(error, "Impossible de charger les livres"));
    } finally {
      loading.value = false;
    }
  }

  async function remove(livre: LivreResponse) {
    actionLoadingId.value = livre.id;

    try {
      await deleteLivre(livre.id);
      push("success", "Livre supprimé avec succès");
      await fetchLivres();
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      push("error", extractHttpErrorMessage(error, "Impossible de supprimer ce livre"));
    } finally {
      actionLoadingId.value = null;
    }
  }

  // ==============================
  // Confirmation avant suppression
  // ==============================

  const confirmTitle = computed(() => "Supprimer ce livre ?");

  const confirmMessage = computed(() => {
    const livre = pendingDelete.value;
    if (!livre) return "";
    return `"${livre.titre}" sera définitivement supprimé du catalogue.\nCette action est irréversible.`;
  });

  const isConfirming = computed(() => actionLoadingId.value !== null);

  function askRemove(livre: LivreResponse) {
    pendingDelete.value = livre;
  }

  function cancelPendingDelete() {
    pendingDelete.value = null;
  }

  async function confirmPendingDelete() {
    const livre = pendingDelete.value;
    if (!livre) return;
    await remove(livre);
    pendingDelete.value = null;
  }

  return {
    loading,
    actionLoadingId,
    items,
    paginatedItems,
    currentPage,
    pageSize,
    pageSizeOptions,
    totalItems,
    totalPages,
    fetchLivres,
    remove,
    goToPage,
    setPageSize,

    pendingDelete,
    confirmTitle,
    confirmMessage,
    isConfirming,
    askRemove,
    confirmPendingDelete,
    cancelPendingDelete,
  };
}
