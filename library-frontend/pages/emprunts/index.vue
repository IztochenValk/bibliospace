<template>
  <PageShell
    title="Emprunts"
    :subtitle="isStaff ? 'Liste et recherche transversale des emprunts' : 'Mes emprunts'"
  >
    <div class="page-panel space-y-6">

      <!-- Barre de filtres -->
      <div class="card border border-base-300 bg-base-100">
        <div class="card-body">
          <div class="grid grid-cols-1 gap-4 md:grid-cols-12">

            <!-- Nom / Prénom (staff uniquement) -->
            <div v-if="isStaff" class="form-control md:col-span-4">
              <label class="label" for="filter-nom">
                <span class="label-text">Nom ou prénom de l'adhérent</span>
              </label>
              <input
                id="filter-nom"
                v-model="nomFilter"
                type="text"
                class="input input-bordered w-full"
                placeholder="ex : Martin"
                autocomplete="off"
              />
            </div>

            <!-- Titre -->
            <div :class="[
              'form-control',
              isStaff ? 'md:col-span-4' : 'md:col-span-8',
            ]">
              <label class="label" for="filter-titre">
                <span class="label-text">Titre du livre</span>
              </label>
              <input
                id="filter-titre"
                v-model="titreFilter"
                type="text"
                class="input input-bordered w-full"
                placeholder="ex : Le Petit Prince"
                autocomplete="off"
              />
            </div>

            <!-- Statut -->
            <div class="form-control md:col-span-4">
              <label class="label" for="filter-statut">
                <span class="label-text">Statut</span>
              </label>
              <select
                id="filter-statut"
                v-model="statutFilter"
                class="select select-bordered w-full"
              >
                <option value="TOUS">Tous</option>
                <option value="EN_COURS">En cours</option>
                <option value="RETOURNE">Retournés</option>
              </select>
            </div>
          </div>

          <!-- Actions -->
          <div class="mt-4 flex flex-wrap items-center gap-3">
            <button
              type="button"
              class="btn btn-ghost btn-sm"
              :disabled="loading"
              @click="resetFilters"
            >
              Réinitialiser les filtres
            </button>

            <p v-if="!loading" class="text-sm opacity-70">
              {{ items.length }} emprunt(s) affiché(s)
            </p>
          </div>
        </div>
      </div>

      <!-- Message d'erreur -->
      <div v-if="errorMessage" class="alert alert-error">
        <span>{{ errorMessage }}</span>
      </div>

      <!-- Tableau -->
      <EmpruntTable
        :items="items"
        :loading="loading"
        :action-loading-id="actionLoadingId"
        :error-message="''"
        :show-retour-action="isStaff"
        @refresh="fetchEmprunts"
        @retour="retour"
      />
    </div>
  </PageShell>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import PageShell from "~/components/app/PageShell.vue";
import EmpruntTable from "~/components/emprunts/EmpruntTable.vue";
import { useEmpruntsPage } from "~/composables/pages/useEmpruntsPage";

definePageMeta({
  layout: "default",
  middleware: ["auth"],
});

const auth = useAuth();
await auth.ensureHydrated();
await auth.ensureProfileLoaded();

const {
  items,
  loading,
  actionLoadingId,
  errorMessage,
  isStaff,
  nomFilter,
  titreFilter,
  statutFilter,
  fetchEmprunts,
  retour,
  resetFilters,
} = useEmpruntsPage();

onMounted(fetchEmprunts);
</script>
