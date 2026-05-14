<template>
  <NuxtLink
    v-if="isAuthed && role === 'ADHERENT'"
    class="btn btn-ghost btn-sm"
    to="/emprunts"
  >
    Mes emprunts
  </NuxtLink>
</template>

<script setup lang="ts">
import type { Role } from "~/types";

defineProps<{
  isAuthed: boolean;
  role: Role | null;
}>();
</script>
