<template>
  <div class="card bg-base-100 border border-base-300">
    <div class="card-body space-y-4">
      <div class="flex justify-end">
        <button
          class="btn btn-ghost"
          type="button"
          :disabled="loading"
          @click="$emit('refresh')"
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
        Aucun emprunt.
      </div>

      <div v-else class="overflow-x-auto">
        <table class="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Livre</th>
              <th>Utilisateur</th>
              <th>Date emprunt</th>
              <th>Date retour prévue</th>
              <th>Date retour effective</th>
              <th>Statut</th>
              <th class="text-right">Actions</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="item in items" :key="item.id">
              <td>#{{ item.id }}</td>
              <td>{{ item.titreLivre ?? "Sans titre" }}</td>
              <td>{{ formatUser(item.nomUtilisateur, item.prenomUtilisateur) }}</td>
              <td>{{ formatDate(item.dateEmprunt) }}</td>
              <td>{{ formatDate(item.dateRetourPrevue) }}</td>
              <td>{{ formatDate(item.dateRetourEffective) }}</td>
              <td>
                <EmpruntStatusBadge :statut="item.statut" />
              </td>
              <td class="text-right">
                <button
                  v-if="canReturn(item)"
                  class="btn btn-sm btn-primary"
                  type="button"
                  :disabled="actionLoadingId === item.id"
                  @click="$emit('return', item)"
                >
                  {{ actionLoadingId === item.id ? "..." : "Retourner" }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import EmpruntStatusBadge from "~/components/emprunts/EmpruntStatusBadge.vue";
import type { EmpruntResponse } from "~/types/emprunts";
import { formatDate } from "~/utils/formatters/dateFormatter";
import { formatUser } from "~/utils/formatters/userFormatter";

defineProps<{
  items: EmpruntResponse[];
  loading: boolean;
  errorMessage: string;
  actionLoadingId: number | null;
  canReturn: (item: EmpruntResponse) => boolean;
}>();

defineEmits<{
  (e: "refresh"): void;
  (e: "return", item: EmpruntResponse): void;
}>();
</script>
