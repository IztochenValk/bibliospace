<template>
  <PageShell
    title="Détail livre"
    subtitle="Consultation d'un livre"
  >
    <div v-if="loading" class="text-base-content/60">
      Chargement...
    </div>

    <div v-else-if="errorMessage" class="alert alert-error">
      <span>{{ errorMessage }}</span>
    </div>

    <LivreDetailsCard
      v-else-if="livre"
      :livre="livre"
    />

    <div v-else class="text-base-content/60">
      Aucun livre trouvé.
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import PageShell from "~/components/app/PageShell.vue";
import LivreDetailsCard from "~/components/livres/LivreDetailsCard.vue";
import { useLivreDetailPage } from "~/composables/pages/useLivreDetailPage";

definePageMeta({
  layout: "default",
  middleware: ["auth"],
});

const route = useRoute();
const id = Number(route.params.id);

const {
  livre,
  loading,
  errorMessage,
  fetchLivre,
} = useLivreDetailPage();

onMounted(async () => {
  if (!Number.isFinite(id) || id <= 0) {
    return;
  }

  await fetchLivre(id);
});
</script>
