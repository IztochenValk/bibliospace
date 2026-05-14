<template>
  <PageShell
    title="Créer un livre"
    subtitle="Ajout d'un nouveau livre"
  >
    <div class="card bg-base-100 border border-base-300">
      <div class="card-body">
        <LivreForm
          :model-value="initialForm"
          :loading="saving"
          :error-message="errorMessage"
          submit-label="Créer"
          @submit="onSubmit"
        />
      </div>
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import PageShell from "~/components/app/PageShell.vue";
import LivreForm from "~/components/livres/LivreForm.vue";
import { useLivreCreatePage } from "~/composables/pages/useLivreCreatePage";
import type { LivreFormValue } from "~/types/livres";

definePageMeta({
  layout: "default",
  middleware: ["bibliothecaire"],
});

const {
  saving,
  errorMessage,
  initialForm,
  submit,
} = useLivreCreatePage();

async function onSubmit(value: LivreFormValue) {
  await submit(value);
}
</script>