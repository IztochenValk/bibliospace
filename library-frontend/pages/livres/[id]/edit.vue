<template>
  <PageShell
    title="Modifier livre"
    subtitle="Mise à jour des informations du livre"
  >
    <div v-if="loading" class="text-base-content/60">
      Chargement...
    </div>

    <div v-else-if="errorMessage && !livre" class="alert alert-error">
      <span>{{ errorMessage }}</span>
    </div>

    <div
      v-else-if="livre"
      class="card bg-base-100 border border-base-300"
    >
      <div class="card-body">
        <LivreForm
          :model-value="initialForm"
          :loading="saving"
          :error-message="errorMessage"
          submit-label="Mettre à jour"
          @submit="onSubmit"
        />
      </div>
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import PageShell from "~/components/app/PageShell.vue";
import LivreForm from "~/components/livres/LivreForm.vue";
import { useLivreEditPage } from "~/composables/pages/useLivreEditPage";
import type { LivreFormValue } from "~/types/livres";

definePageMeta({
  layout: "default",
  middleware: ["bibliothecaire"],
});

const route = useRoute();
const id = Number(route.params.id);

const {
  livre,
  loading,
  saving,
  errorMessage,
  initialForm,
  fetchLivre,
  submit,
} = useLivreEditPage();

onMounted(async () => {
  if (!Number.isFinite(id) || id <= 0) {
    return;
  }

  await fetchLivre(id);
});

async function onSubmit(value: LivreFormValue) {
  if (!Number.isFinite(id) || id <= 0) {
    return;
  }

  await submit(id, value);
}
</script>
