<template>
  <PageShell
    title="Catégories"
    subtitle="Liste des catégories disponibles"
  >
    <div class="page-panel">
      <CategorieTable
        :items="items"
        :loading="loading"
        :action-loading-id="actionLoadingId"
        :error-message="errorMessage"
        @refresh="fetchCategories"
        @delete="askRemove"
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
import ConfirmModal from "~/components/app/ConfirmModal.vue";
import CategorieTable from "~/components/categories/CategorieTable.vue";
import { useCategoriesPage } from "~/composables/pages/useCategoriesPage";

definePageMeta({
  layout: "default",
  middleware: ["bibliothecaire"],
});

const {
  items,
  loading,
  actionLoadingId,
  errorMessage,
  fetchCategories,

  pendingDelete,
  confirmTitle,
  confirmMessage,
  isConfirming,
  askRemove,
  confirmPendingDelete,
  cancelPendingDelete,
} = useCategoriesPage();

const confirmModalRef = ref<{ open: () => void; close: () => void } | null>(null);

watch(pendingDelete, (value) => {
  if (value) {
    confirmModalRef.value?.open();
  } else {
    confirmModalRef.value?.close();
  }
});

onMounted(fetchCategories);
</script>
