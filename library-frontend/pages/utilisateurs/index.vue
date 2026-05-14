<template>
  <PageShell
    title="Utilisateurs"
    subtitle="Gestion des utilisateurs"
  >
    <UtilisateurTable
      :items="paginatedItems"
      :loading="loading"
      :action-loading-id="actionLoadingId"
      :can-delete="canDelete"
      :can-reactivate="canReactivate"
      :can-anonymize="canAnonymize"
      :can-edit="canEdit"
      @refresh="fetchUtilisateurs"
      @delete="askRemove"
      @reactivate="askReactivate"
      @anonymize="askAnonymize"
    />

    <AppPagination
      :current-page="currentPage"
      :page-size="pageSize"
      :total-items="totalItems"
      :total-pages="totalPages"
      :page-size-options="pageSizeOptions"
      @change-page="goToPage"
      @change-page-size="setPageSize"
    />

    <ConfirmModal
      ref="confirmModalRef"
      :title="confirmTitle"
      :message="confirmMessage"
      :variant="confirmVariant"
      :confirm-label="confirmLabel"
      :loading="isConfirming"
      @confirm="confirmPendingAction"
      @cancel="cancelPendingAction"
    />
  </PageShell>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import PageShell from "~/components/app/PageShell.vue";
import AppPagination from "~/components/app/AppPagination.vue";
import ConfirmModal from "~/components/app/ConfirmModal.vue";
import UtilisateurTable from "~/components/utilisateurs/UtilisateurTable.vue";
import { useUtilisateursPage } from "~/composables/pages/useUtilisateursPage";

definePageMeta({
  layout: "default",
  middleware: ["bibliothecaire"],
});

const {
  loading,
  actionLoadingId,
  paginatedItems,
  currentPage,
  pageSize,
  pageSizeOptions,
  totalItems,
  totalPages,
  fetchUtilisateurs,
  canDelete,
  canReactivate,
  canAnonymize,
  canEdit,
  goToPage,
  setPageSize,

  pendingAction,
  confirmTitle,
  confirmMessage,
  confirmVariant,
  confirmLabel,
  isConfirming,
  askRemove,
  askReactivate,
  askAnonymize,
  confirmPendingAction,
  cancelPendingAction,
} = useUtilisateursPage();

const confirmModalRef = ref<{ open: () => void; close: () => void } | null>(null);

// Ouvre/ferme la modale en fonction de l'action en attente.
watch(pendingAction, (value) => {
  if (value) {
    confirmModalRef.value?.open();
  } else {
    confirmModalRef.value?.close();
  }
});

onMounted(async () => {
  await fetchUtilisateurs();
});
</script>
