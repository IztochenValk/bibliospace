<template>
  <PageShell
    title="Catalogue"
    subtitle="Liste des livres disponibles"
  >
    <div class="card bg-base-100 border border-base-300">
      <div class="card-body space-y-4">
        <!-- Bouton Rafraîchir aligné à droite. La navigation vers les
             emprunts (adhérent ou staff) est dans la topbar globale,
             on ne dédouble pas ici pour éviter la confusion visuelle
             où "Mes emprunts" pouvait être pris pour le titre de section. -->
        <div class="flex justify-end">
          <button
            class="btn btn-ghost"
            type="button"
            :disabled="loading"
            @click="fetchCatalogue"
          >
            {{ loading ? "Chargement..." : "Rafraîchir" }}
          </button>
        </div>

        <div v-if="loading" class="text-base-content/60">
          Chargement...
        </div>

        <div v-else-if="errorMessage" class="alert alert-error">
          <span>{{ errorMessage }}</span>
        </div>

        <div v-else-if="items.length === 0" class="text-base-content/60">
          Aucun livre dans le catalogue.
        </div>

        <div v-else class="overflow-x-auto">
          <table class="table">
            <thead>
              <tr>
                <th>ID livre</th>
                <th>Titre</th>
                <th>Auteur</th>
                <th>Quantité totale</th>
                <th>Quantité disponible</th>
                <th>Déjà emprunté</th>
                <th class="text-right">Action</th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="item in items" :key="item.livreId">
                <td>#{{ item.livreId }}</td>
                <td>
                  <NuxtLink
                    :to="`/livres/${item.livreId}`"
                    style="color: #60a5fa; text-decoration: underline; cursor: pointer; font-weight: 500;"
                  >
                    {{ item.titre ?? "Sans titre" }}
                  </NuxtLink>
                </td>
                <td>{{ item.auteur ?? "Inconnu" }}</td>
                <td>{{ item.quantiteTotale }}</td>
                <td>{{ item.quantiteDisponible }}</td>
                <td>
                  <span
                    class="badge"
                    :class="item.emprunteParUtilisateur ? 'badge-warning' : 'badge-success'"
                  >
                    {{ item.emprunteParUtilisateur ? "Oui" : "Non" }}
                  </span>
                </td>
                <td class="text-right">
                  <div class="flex justify-end gap-2">
                    <NuxtLink
                      :to="`/livres/${item.livreId}`"
                      class="btn btn-sm btn-ghost"
                    >
                      Voir
                    </NuxtLink>
                    <button
                      class="btn btn-sm btn-primary"
                      type="button"
                      :disabled="!canBorrow(item)"
                      @click="openBorrowModal(item)"
                    >
                      Emprunter
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <EmpruntCreateModal
      ref="borrowModalRef"
      :item="selectedItem"
      :loading="borrowLoading"
      :error-message="borrowErrorMessage"
      @submit="submitBorrow"
    />
  </PageShell>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import PageShell from "~/components/app/PageShell.vue";
import EmpruntCreateModal from "~/components/emprunts/EmpruntCreateModal.vue";
import { useCataloguePage } from "~/composables/pages/useCataloguePage";

definePageMeta({
  layout: "default",
  middleware: ["auth"],
});

const {
  items,
  loading,
  errorMessage,
  fetchCatalogue,
  borrowModalRef,
  selectedItem,
  borrowLoading,
  borrowErrorMessage,
  canBorrow,
  openBorrowModal,
  submitBorrow,
} = useCataloguePage();

onMounted(fetchCatalogue);
</script>
