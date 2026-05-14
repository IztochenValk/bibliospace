<template>
  <form class="space-y-6" @submit.prevent="onSubmit">
    <div class="grid gap-4 md:grid-cols-2">
      <label class="form-control w-full md:col-span-2">
        <div class="label">
          <span class="label-text">Ancien mot de passe</span>
        </div>
        <input
          v-model="ancienMotDePasse"
          :type="showPassword ? 'text' : 'password'"
          class="input input-bordered w-full"
          :disabled="loading"
          autocomplete="current-password"
        />
      </label>

      <label class="form-control w-full md:col-span-2">
        <div class="label">
          <span class="label-text">Nouveau mot de passe</span>
        </div>
        <input
          v-model="nouveauMotDePasse"
          :type="showPassword ? 'text' : 'password'"
          class="input input-bordered w-full"
          :disabled="loading"
          autocomplete="new-password"
        />
      </label>

      <label class="form-control w-full md:col-span-2">
        <div class="label">
          <span class="label-text">Confirmer le nouveau mot de passe</span>
        </div>
        <input
          v-model="confirmPassword"
          :type="showPassword ? 'text' : 'password'"
          class="input input-bordered w-full"
          :disabled="loading"
          autocomplete="new-password"
        />
      </label>

      <label class="label cursor-pointer justify-start gap-3 md:col-span-2">
        <input
          v-model="showPassword"
          type="checkbox"
          class="checkbox checkbox-sm"
          :disabled="loading"
        />
        <span class="label-text">Afficher le mot de passe</span>
      </label>
    </div>

    <div
      v-if="errorMessage"
      class="alert alert-error"
    >
      <span>{{ errorMessage }}</span>
    </div>

    <div class="flex justify-end">
      <button
        class="btn btn-primary"
        type="submit"
        :disabled="loading || !canSubmit"
      >
        {{ loading ? "Enregistrement..." : "Changer le mot de passe" }}
      </button>
    </div>
  </form>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";

const props = withDefaults(defineProps<{
  loading?: boolean;
  errorMessage?: string;
}>(), {
  loading: false,
  errorMessage: "",
});

const emit = defineEmits<{
  (e: "submit", value: { ancienMotDePasse: string; nouveauMotDePasse: string }): void;
}>();

const ancienMotDePasse = ref("");
const nouveauMotDePasse = ref("");
const confirmPassword = ref("");
const showPassword = ref(false);

const canSubmit = computed(() => {
  return !!ancienMotDePasse.value.trim()
    && !!nouveauMotDePasse.value.trim()
    && !!confirmPassword.value.trim()
    && nouveauMotDePasse.value === confirmPassword.value
    && ancienMotDePasse.value !== nouveauMotDePasse.value;
});

function onSubmit() {
  emit("submit", {
    ancienMotDePasse: ancienMotDePasse.value,
    nouveauMotDePasse: nouveauMotDePasse.value,
  });

  ancienMotDePasse.value = "";
  nouveauMotDePasse.value = "";
  confirmPassword.value = "";
  showPassword.value = false;
}
</script>
