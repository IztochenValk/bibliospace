<template>
  <section class="page-frame space-y-6">
    <div class="space-y-2">
      <h1 class="text-3xl font-bold text-base-content">
        {{ title }}
      </h1>

      <p
        v-if="subtitle"
        class="text-sm text-base-content/70"
      >
        {{ subtitle }}
      </p>
    </div>

    <slot />
  </section>
</template>

<script setup lang="ts">
defineProps<{
  title: string;
  subtitle?: string;
}>();
</script>
