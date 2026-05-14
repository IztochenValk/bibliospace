<template>
  <PageShell
    title="Mon compte"
    subtitle="Modification de vos informations personnelles"
  >
    <div v-if="loading" class="text-base-content/60">
      Chargement...
    </div>

    <div v-else-if="errorMessage && !utilisateur" class="alert alert-error">
      <span>{{ errorMessage }}</span>
    </div>

    <template v-else-if="utilisateur">
      <div class="grid gap-6 xl:grid-cols-2 items-start">
        <div class="card bg-base-100 border border-base-300 h-full">
          <div class="card-body space-y-4">
            <div class="flex items-start justify-between gap-4">
              <div>
                <h2 class="card-title">
                  Informations du compte
                </h2>
                <p class="text-sm text-base-content/60">
                  Modifiez votre nom, prénom et email.
                </p>
              </div>

              <span class="badge badge-outline">
                {{ utilisateur.role }}
              </span>
            </div>

            <UtilisateurForm
              :model-value="initialForm"
              :loading="saving"
              :error-message="errorMessage"
              :lock-role="true"
              :allowed-roles="[utilisateur.role]"
              @submit="onSubmitProfile"
            />
          </div>
        </div>

        <div class="card bg-base-100 border border-base-300 h-full">
          <div class="card-body space-y-4">
            <div>
              <h2 class="card-title">
                Mot de passe
              </h2>
              <p class="text-sm text-base-content/60">
                Changez votre mot de passe depuis cette page.
              </p>
            </div>

            <MePasswordForm
              :loading="passwordSaving"
              :error-message="passwordErrorMessage"
              @submit="onSubmitPassword"
            />
          </div>
        </div>
      </div>
    </template>
  </PageShell>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import PageShell from "~/components/app/PageShell.vue";
import UtilisateurForm from "~/components/utilisateurs/UtilisateurForm.vue";
import MePasswordForm from "~/components/me/MePasswordForm.vue";
import { useMePage } from "~/composables/pages/useMePage";

definePageMeta({
  layout: "default",
  middleware: ["auth"],
});

type ProfileFormValue = {
  nom: string;
  prenom: string;
  email: string;
  role: "ADHERENT" | "BIBLIOTHECAIRE" | "ADMINISTRATEUR";
};

type PasswordFormValue = {
  ancienMotDePasse: string;
  nouveauMotDePasse: string;
};

const {
  utilisateur,
  loading,
  saving,
  passwordSaving,
  errorMessage,
  passwordErrorMessage,
  initialForm,
  fetchMeProfile,
  submitProfile,
  submitPassword,
} = useMePage();

onMounted(fetchMeProfile);

async function onSubmitProfile(value: ProfileFormValue) {
  await submitProfile({
    nom: value.nom,
    prenom: value.prenom,
    email: value.email,
  });
}

async function onSubmitPassword(value: PasswordFormValue) {
  await submitPassword(value);
}
</script>
