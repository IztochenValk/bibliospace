<template>
  <PageShell
    title="Créer une catégorie"
    subtitle="Ajout d'une nouvelle catégorie"
  >
    <div class="card bg-base-100 border border-base-300">
      <div class="card-body">
        <CategorieForm
          :model-value="initialForm"
          :loading="saving"
          :error-message="errorMessage"
          submit-label="Créer"
          submit-loading-label="Création..."
          @submit="onSubmit"
        />
      </div>
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import PageShell from "~/components/app/PageShell.vue";
import CategorieForm from "~/components/categories/CategorieForm.vue";
import { useCategorieCreatePage } from "~/composables/pages/useCategorieCreatePage";

definePageMeta({
  layout: "default",
  middleware: ["bibliothecaire"],
});

type CategorieFormValue = {
  nomCategorie: string;
};

const {
  saving,
  errorMessage,
  initialForm,
  submit,
} = useCategorieCreatePage();

async function onSubmit(value: CategorieFormValue) {
  await submit(value);
}
</script>
