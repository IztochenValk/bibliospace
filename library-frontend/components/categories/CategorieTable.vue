<template>
  <div class="card bg-base-100 border border-base-300">
    <div class="card-body space-y-4">
      <div class="flex flex-wrap justify-between gap-3">
        <NuxtLink class="btn btn-primary" to="/categories/create">
          Nouvelle catégorie
        </NuxtLink>

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
        Aucune catégorie.
      </div>

      <div v-else class="overflow-x-auto">
        <table class="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nom</th>
              <th>Utilisée</th>
              <th class="text-right">Actions</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="item in items" :key="item.id">
              <td>#{{ item.id }}</td>
              <td>{{ item.nomCategorie }}</td>

              <td>
                <span
                  class="badge"
                  :class="item.utilisee ? 'badge-warning' : 'badge-success'"
                >
                  {{ item.utilisee ? "Oui" : "Non" }}
                </span>
              </td>

              <td class="text-right">
                <div class="flex justify-end gap-2">
                  <NuxtLink
                    class="btn btn-sm btn-primary"
                    :to="`/categories/${item.id}/edit`"
                  >
                    Modifier
                  </NuxtLink>

                  <button
                    class="btn btn-sm btn-error"
                    type="button"
                    :disabled="item.utilisee || actionLoadingId === item.id"
                    :title="item.utilisee ? 'Impossible de supprimer une catégorie liée à un livre' : 'Supprimer la catégorie'"
                    @click="$emit('delete', item)"
                  >
                    {{ actionLoadingId === item.id ? "..." : "Supprimer" }}
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="text-xs text-base-content/50">
        Une catégorie liée à un livre ne peut pas être supprimée.
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Categorie } from "~/types/categories";

withDefaults(defineProps<{
  items: Categorie[];
  loading: boolean;
  errorMessage?: string;
  actionLoadingId?: number | null;
}>(), {
  errorMessage: "",
  actionLoadingId: null,
});

defineEmits<{
  (e: "refresh"): void;
  (e: "delete", item: Categorie): void;
}>();
</script>