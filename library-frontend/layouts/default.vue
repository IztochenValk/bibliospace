<template>
  <div class="min-h-screen flex flex-col text-base-content">
    <AppTopbar />

    <main class="flex-1">
      <div class="mx-auto w-full max-w-7xl px-4 py-8">
        <slot />
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<script setup lang="ts">
import AppTopbar from "~/components/app/AppTopbar.vue";
import AppFooter from "~/components/app/AppFooter.vue";
</script>
