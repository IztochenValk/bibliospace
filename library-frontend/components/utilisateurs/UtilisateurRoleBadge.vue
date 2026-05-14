<template>
  <span class="badge badge-outline" :class="badgeClass">
    {{ role }}
  </span>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { Role } from "~/types/shared";
import type { Role as RoleType } from "~/types/shared";

const props = defineProps<{
  role: RoleType;
}>();

const badgeClass = computed(() => {
  if (props.role === Role.ADMINISTRATEUR) {
    return "border-error text-error";
  }

  if (props.role === Role.BIBLIOTHECAIRE) {
    return "border-warning text-warning";
  }

  return "border-info text-info";
});
</script>
