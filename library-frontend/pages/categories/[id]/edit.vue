<template>
  <PageShell
    title="Modifier la catégorie"
    subtitle="Mise à jour du nom de la catégorie"
  >
    <div v-if="loading" class="text-base-content/60">
      Chargement...
    </div>

    <div v-else-if="errorMessage && !categorie" class="alert alert-error">
      <span>{{ errorMessage }}</span>
    </div>

    <div
      v-else-if="categorie"
      class="card bg-base-100 border border-base-300"
    >
      <div class="card-body">
        <CategorieForm
          :model-value="initialForm"
          :loading="saving"
          :error-message="errorMessage"
          submit-label="Mettre à jour"
          submit-loading-label="Mise à jour..."
          @submit="onSubmit"
        />
      </div>
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import PageShell from "~/components/app/PageShell.vue";
import CategorieForm from "~/components/categories/CategorieForm.vue";
import { useCategorieEditPage } from "~/composables/pages/useCategorieEditPage";

definePageMeta({
  layout: "default",
  middleware: ["bibliothecaire"],
});

type CategorieFormValue = {
  nomCategorie: string;
};

const route = useRoute();
const id = Number(route.params.id);

const {
  categorie,
  loading,
  saving,
  errorMessage,
  initialForm,
  fetchCategorie,
  submit,
} = useCategorieEditPage();

onMounted(async () => {
  if (!Number.isFinite(id) || id <= 0) {
    return;
  }
  await fetchCategorie(id);
});

async function onSubmit(value: CategorieFormValue) {
  if (!Number.isFinite(id) || id <= 0) {
    return;
  }
  await submit(id, value);
}
</script>
