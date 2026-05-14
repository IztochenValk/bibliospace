<template>
  <div class="card bg-base-100 border border-base-300">
    <div class="card-body space-y-4">
      <div class="flex flex-wrap justify-between gap-3">
        <NuxtLink class="btn btn-primary" to="/livres/create">
          Nouveau livre
        </NuxtLink>

        <button class="btn btn-ghost" type="button" @click="$emit('refresh')">
          Rafraîchir
        </button>
      </div>

      <div v-if="loading" class="text-base-content/60">
        Chargement...
      </div>

      <div v-else-if="items.length === 0" class="text-base-content/60">
        Aucun livre.
      </div>

      <div v-else class="overflow-x-auto">
        <table class="table">
          <thead>
            <tr>
              <th>Livre</th>
              <th>Auteur</th>
              <th>Langue</th>
              <th>Stock</th>
              <th class="text-right">Actions</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="livre in items" :key="livre.id">
              <td>
                <div class="flex items-center gap-3">
                  <div class="h-16 w-12 overflow-hidden rounded border border-base-300 bg-base-200">
                    <img
                      v-if="resolveImageUrl(livre.imageUrl)"
                      :src="resolveImageUrl(livre.imageUrl)"
                      :alt="livre.titre"
                      class="h-full w-full object-cover"
                    />
                    <div v-else class="flex h-full w-full items-center justify-center text-xs text-base-content/40">
                      Aucune image
                    </div>
                  </div>

                  <div>
                    <div class="font-medium">{{ livre.titre }}</div>
                    <div class="text-xs text-base-content/60">#{{ livre.id }}</div>
                  </div>
                </div>
              </td>

              <td>{{ livre.auteur }}</td>
              <td>{{ livre.langue || "-" }}</td>
              <td>
                <span class="font-mono">
                  {{ livre.quantiteDisponible ?? 0 }} / {{ livre.quantiteTotale ?? 0 }}
                </span>
                <div class="text-xs text-base-content/60">
                  disponible / total
                </div>
              </td>

              <td class="text-right">
                <div class="flex justify-end gap-2">
                  <NuxtLink
                    class="btn btn-sm btn-ghost"
                    :to="`/livres/${livre.id}`"
                  >
                    Voir
                  </NuxtLink>

                  <NuxtLink
                    class="btn btn-sm btn-primary"
                    :to="`/livres/${livre.id}/edit`"
                  >
                    Modifier
                  </NuxtLink>

                  <button
                    class="btn btn-error btn-sm"
                    type="button"
                    :disabled="actionLoadingId === livre.id"
                    @click="$emit('delete', livre)"
                  >
                    {{ actionLoadingId === livre.id ? "..." : "Supprimer" }}
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { LivreResponse } from "~/types/livres";
import { useImageUrl } from "~/composables/utils/useImageUrl";

defineProps<{
  items: LivreResponse[];
  loading: boolean;
  actionLoadingId: number | null;
}>();

defineEmits<{
  (e: "refresh"): void;
  (e: "delete", livre: LivreResponse): void;
}>();

const { resolveImageUrl } = useImageUrl();
</script>
