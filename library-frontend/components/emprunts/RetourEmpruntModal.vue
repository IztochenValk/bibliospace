<template>
  <dialog class="modal" :class="{ 'modal-open': open }">
    <div class="modal-box">
      <h3 class="text-lg font-bold">
        Rendre l'emprunt
      </h3>

      <p class="mt-2 text-sm text-base-content/70">
        Confirme le retour pour :
        <span class="font-semibold">{{ emprunt?.titreLivre || "Livre inconnu" }}</span>
      </p>

      <div class="mt-4">
        <label class="form-control w-full">
          <div class="label">
            <span class="label-text">Date de retour effective</span>
          </div>
          <input
            v-model="dateRetourEffective"
            type="date"
            class="input input-bordered w-full"
            :max="today"
            :disabled="loading"
          />
        </label>
      </div>

      <div
        v-if="errorMessage"
        class="alert alert-error mt-4"
      >
        <span>{{ errorMessage }}</span>
      </div>

      <div class="modal-action">
        <button
          class="btn btn-ghost"
          type="button"
          :disabled="loading"
          @click="$emit('close')"
        >
          Annuler
        </button>

        <button
          class="btn btn-primary"
          type="button"
          :disabled="loading || !dateRetourEffective"
          @click="submit"
        >
          {{ loading ? "Retour..." : "Confirmer" }}
        </button>
      </div>
    </div>

    <form method="dialog" class="modal-backdrop">
      <button type="button" @click="$emit('close')">close</button>
    </form>
  </dialog>
</template>

<script setup lang="ts">
import type { EmpruntResponse } from "~/types/emprunts";

const props = defineProps<{
  open: boolean;
  emprunt: EmpruntResponse | null;
  loading: boolean;
  errorMessage?: string;
}>();

const emit = defineEmits<{
  (e: "close"): void;
  (e: "confirm", payload: { dateRetourEffective: string }): void;
}>();

const today = new Date().toISOString().slice(0, 10);
const dateRetourEffective = ref(today);

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      dateRetourEffective.value = today;
    }
  }
);

function submit() {
  emit("confirm", {
    dateRetourEffective: dateRetourEffective.value,
  });
}
</script>
