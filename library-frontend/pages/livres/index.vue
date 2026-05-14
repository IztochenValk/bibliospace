<template>
  <PageShell title="Livres" subtitle="Liste des livres">
    <div class="page-panel">
      <LivreTable
        :items="paginatedItems"
        :loading="loading"
        :action-loading-id="actionLoadingId"
        @refresh="fetchLivres"
        @delete="askRemove"
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
        variant="danger"
        confirm-label="Supprimer"
        :loading="isConfirming"
        @confirm="confirmPendingDelete"
        @cancel="cancelPendingDelete"
      />
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import PageShell from "~/components/app/PageShell.vue";
import AppPagination from "~/components/app/AppPagination.vue";
import ConfirmModal from "~/components/app/ConfirmModal.vue";
import LivreTable from "~/components/livres/LivreTable.vue";
import { useLivresPage } from "~/composables/pages/useLivresPage";

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
  fetchLivres,
  goToPage,
  setPageSize,

  pendingDelete,
  confirmTitle,
  confirmMessage,
  isConfirming,
  askRemove,
  confirmPendingDelete,
  cancelPendingDelete,
} = useLivresPage();

const confirmModalRef = ref<{ open: () => void; close: () => void } | null>(null);

watch(pendingDelete, (value) => {
  if (value) {
    confirmModalRef.value?.open();
  } else {
    confirmModalRef.value?.close();
  }
});

onMounted(fetchLivres);
</script>
