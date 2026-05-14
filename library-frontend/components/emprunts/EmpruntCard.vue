<template>
  <div class="card border border-base-300 bg-base-100 shadow-lg">
    <figure class="h-72 overflow-hidden bg-base-200">
      <img
        v-if="resolvedImageUrl"
        :src="resolvedImageUrl"
        :alt="item.titreLivre ?? 'Livre'"
        class="h-full w-full object-cover"
      />
      <div
        v-else
        class="flex h-full w-full items-center justify-center text-sm text-base-content/40"
      >
        Aucune image
      </div>
    </figure>

    <div class="card-body space-y-3">
      <div class="flex items-start justify-between gap-3">
        <h3 class="card-title text-base leading-tight">
          {{ item.titreLivre ?? "Sans titre" }}
        </h3>

        <EmpruntStatusBadge :statut="item.statut" />
      </div>

      <div class="space-y-2 text-sm text-base-content/70">
        <div>
          <span class="font-medium text-base-content">Emprunté le :</span>
          {{ item.dateEmprunt }}
        </div>

        <div>
          <span class="font-medium text-base-content">Retour prévu :</span>
          {{ item.dateRetourPrevue }}
        </div>

        <div v-if="item.dateRetourEffective">
          <span class="font-medium text-base-content">Retour effectif :</span>
          {{ item.dateRetourEffective }}
        </div>
      </div>

      <div class="card-actions justify-end pt-2">
        <button
          v-if="showRetourAction && item.statut === 'EN_COURS'"
          class="btn btn-primary btn-sm"
          type="button"
          :disabled="loading"
          @click="$emit('retour', item)"
        >
          {{ loading ? "..." : "Retourner" }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import EmpruntStatusBadge from "~/components/emprunts/EmpruntStatusBadge.vue";
import type { EmpruntResponse } from "~/types/emprunts";
import { useImageUrl } from "~/composables/utils/useImageUrl";

const props = withDefaults(defineProps<{
  item: EmpruntResponse;
  loading?: boolean;
  showRetourAction?: boolean;
}>(), {
  loading: false,
  showRetourAction: true,
});

defineEmits<{
  (e: "retour", item: EmpruntResponse): void;
}>();

const { resolveImageUrl } = useImageUrl();

const resolvedImageUrl = computed(() => resolveImageUrl(props.item.imageUrl));
</script>
