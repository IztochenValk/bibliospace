<template>
  <PageShell
    title="Modifier utilisateur"
    subtitle="Mise à jour des informations principales"
  >
    <div v-if="loading" class="text-base-content/60">
      Chargement...
    </div>

    <div v-else-if="errorMessage && !utilisateur" class="alert alert-error">
      <span>{{ errorMessage }}</span>
    </div>

    <div
      v-else-if="utilisateur"
      class="card bg-base-100 border border-base-300"
    >
      <div class="card-body">
        <UtilisateurForm
          :model-value="initialForm"
          :loading="saving"
          :error-message="errorMessage"
          :lock-role="lockRole"
          :allowed-roles="allowedRoles"
          @submit="onSubmit"
        />
      </div>
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { computed, onMounted } from "vue";
import PageShell from "~/components/app/PageShell.vue";
import UtilisateurForm from "~/components/utilisateurs/UtilisateurForm.vue";
import { useUtilisateurEditPage } from "~/composables/pages/useUtilisateurEditPage";
import { useUtilisateurEditPolicies } from "~/composables/pages/useUtilisateurEditPolicies";

definePageMeta({
  layout: "default",
  middleware: ["bibliothecaire"],
});

type UtilisateurFormValue = {
  nom: string;
  prenom: string;
  email: string;
  role: "ADHERENT" | "BIBLIOTHECAIRE" | "ADMINISTRATEUR";
};

const route = useRoute();
const id = computed(() => Number(route.params.id));

const {
  utilisateur,
  loading,
  saving,
  errorMessage,
  initialForm,
  fetchUtilisateur,
  submit,
} = useUtilisateurEditPage();

const {
  lockRole,
  allowedRoles,
  validateRoleChange,
} = useUtilisateurEditPolicies(id, utilisateur);

onMounted(async () => {
  if (!Number.isFinite(id.value) || id.value <= 0) {
    return;
  }

  await fetchUtilisateur(id.value);
});

async function onSubmit(value: UtilisateurFormValue) {
  if (!Number.isFinite(id.value) || id.value <= 0) {
    return;
  }

  if (!validateRoleChange(value.role)) {
    return;
  }

  await submit(id.value, value);
}
</script>
