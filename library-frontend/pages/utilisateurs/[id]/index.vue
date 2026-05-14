<template>
  <PageShell
    title="Détail utilisateur"
    subtitle="Consultation d'un utilisateur"
  >
    <div v-if="loading" class="text-base-content/60">
      Chargement...
    </div>

    <div v-else-if="errorMessage" class="alert alert-error">
      <span>{{ errorMessage }}</span>
    </div>

    <UtilisateurDetailsCard
      v-else-if="utilisateur"
      :utilisateur="utilisateur"
    />

    <div v-else class="text-base-content/60">
      Aucun utilisateur trouvé.
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import PageShell from "~/components/app/PageShell.vue";
import UtilisateurDetailsCard from "~/components/utilisateurs/UtilisateurDetailsCard.vue";
import { useUtilisateurDetailPage } from "~/composables/pages/useUtilisateurDetailPage";

definePageMeta({
  layout: "default",
  middleware: ["bibliothecaire"],
});

const route = useRoute();
const id = Number(route.params.id);

const {
  utilisateur,
  loading,
  errorMessage,
  fetchUtilisateur,
} = useUtilisateurDetailPage();

onMounted(async () => {
  if (!Number.isFinite(id) || id <= 0) {
    return;
  }

  await fetchUtilisateur(id);
});
</script>
