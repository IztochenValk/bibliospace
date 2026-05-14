import { computed, ref } from "vue";
import { listCatalogue } from "~/services/catalogueService";
import { canBorrowCatalogueItem } from "~/services/catalogue/cataloguePermissions";
import { createEmprunt } from "~/services/empruntsService";
import type { CatalogueItemResponse } from "~/types/catalogue";
import type { ApiThrownError } from "~/services/core/httpErrors";
import { extractCatalogueErrorMessage } from "~/utils/errors/catalogueErrors";
import { extractDefaultApiErrorMessage } from "~/utils/errors/apiErrors";
import { useToast } from "~/composables/ui/useToast";

export function useCataloguePage() {
  const auth = useAuth();
  const { push } = useToast();

  const items = ref<CatalogueItemResponse[]>([]);
  const loading = ref(false);
  const errorMessage = ref("");

  const borrowModalRef = ref<{ open: () => void; close: () => void } | null>(null);
  const selectedItem = ref<CatalogueItemResponse | null>(null);
  const borrowLoading = ref(false);
  const borrowErrorMessage = ref("");

  const currentUserId = computed(() => auth.state.value.userId);
  const currentRole = computed(() => auth.state.value.role);

  async function fetchCatalogue() {
    loading.value = true;
    errorMessage.value = "";

    try {
      items.value = await listCatalogue();
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      errorMessage.value = extractCatalogueErrorMessage(error);
      items.value = [];
    } finally {
      loading.value = false;
    }
  }

  function canBorrow(item: CatalogueItemResponse): boolean {
    return canBorrowCatalogueItem(
      currentRole.value,
      currentUserId.value,
      item
    );
  }

  function openBorrowModal(item: CatalogueItemResponse) {
    if (!canBorrow(item)) {
      return;
    }

    selectedItem.value = item;
    borrowErrorMessage.value = "";
    borrowModalRef.value?.open();
  }

  async function submitBorrow(payload: { livreId: number; dateRetourPrevue: string }) {
    if (!selectedItem.value || !canBorrow(selectedItem.value)) {
      borrowErrorMessage.value = "Action réservée aux adhérents";
      return;
    }

    borrowLoading.value = true;
    borrowErrorMessage.value = "";

    try {
      await createEmprunt({
        livreId: payload.livreId,
        dateRetourPrevue: payload.dateRetourPrevue,
      });

      push("success", "Emprunt créé avec succès");
      borrowModalRef.value?.close();
      await fetchCatalogue();
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      borrowErrorMessage.value = extractDefaultApiErrorMessage(
        error,
        "Impossible de créer l'emprunt"
      );
      push("error", borrowErrorMessage.value);
    } finally {
      borrowLoading.value = false;
    }
  }

  return {
    items,
    loading,
    errorMessage,
    fetchCatalogue,
    borrowModalRef,
    selectedItem,
    borrowLoading,
    borrowErrorMessage,
    canBorrow,
    openBorrowModal,
    submitBorrow,
  };
}
