<template>
  <form class="space-y-4" @submit.prevent="onSubmit">
    <label class="form-control w-full">
      <div class="label">
        <span class="label-text">Date retour effective</span>
      </div>

      <input
        v-model="dateRetourEffective"
        type="date"
        class="input input-bordered w-full"
        :disabled="loading"
      />
    </label>

    <div class="flex justify-end">
      <button
        class="btn btn-primary"
        type="submit"
        :disabled="loading || !dateRetourEffective"
      >
        {{ loading ? "Enregistrement..." : "Valider le retour" }}
      </button>
    </div>
  </form>
</template>

<script setup lang="ts">
import { ref } from "vue";

const props = defineProps<{
  loading?: boolean;
}>();

const emit = defineEmits<{
  (e: "submit", value: { dateRetourEffective: string }): void;
}>();

const dateRetourEffective = ref("");

function onSubmit() {
  emit("submit", {
    dateRetourEffective: dateRetourEffective.value,
  });
}
</script>
