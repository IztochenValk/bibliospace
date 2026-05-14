<template>
  <span class="badge badge-outline" :class="badgeClass">
    {{ statut }}
  </span>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { StatutEmprunt } from "~/types/shared";

const props = defineProps<{
  statut: string;
}>();

const badgeClass = computed(() => {
  if (props.statut === StatutEmprunt.EN_COURS) {
    return "border-warning text-warning";
  }

  if (props.statut === StatutEmprunt.RETOURNE) {
    return "border-success text-success";
  }

  return "border-base-content/30 text-base-content/70";
});
</script>
