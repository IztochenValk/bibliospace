<template>
  <div class="space-y-4">
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
      Aucun emprunt en cours.
    </div>

    <div v-else class="grid gap-6 sm:grid-cols-2 xl:grid-cols-3">
      <EmpruntCard
        v-for="item in items"
        :key="item.id"
        :item="item"
        :loading="actionLoadingId === item.id"
        :show-retour-action="showRetourAction"
        @retour="$emit('retour', item)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import EmpruntCard from "~/components/emprunts/EmpruntCard.vue";
import type { EmpruntResponse } from "~/types/emprunts";

withDefaults(defineProps<{
  items: EmpruntResponse[];
  loading: boolean;
  errorMessage?: string;
  actionLoadingId?: number | null;
  showRetourAction?: boolean;
}>(), {
  errorMessage: "",
  actionLoadingId: null,
  showRetourAction: true,
});

defineEmits<{
  (e: "refresh"): void;
  (e: "retour", item: EmpruntResponse): void;
}>();
</script>
