<template>
  <form class="space-y-6" @submit.prevent="onSubmit">
    <label class="form-control w-full max-w-md">
      <div class="label">
        <span class="label-text">Nom de la catégorie</span>
      </div>

      <input
        v-model="localValue.nomCategorie"
        type="text"
        class="input input-bordered w-full"
        :disabled="loading"
        placeholder="Ex: Poésie"
      />
    </label>

    <div
      v-if="errorMessage"
      class="alert alert-error"
    >
      <span>{{ errorMessage }}</span>
    </div>

    <div class="flex justify-end gap-3">
      <NuxtLink class="btn btn-ghost" to="/categories">
        Annuler
      </NuxtLink>

      <button
        class="btn btn-primary"
        type="submit"
        :disabled="loading || !canSubmit"
      >
        {{ loading ? submitLoadingLabel : submitLabel }}
      </button>
    </div>
  </form>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from "vue";

type CategorieFormValue = {
  nomCategorie: string;
};

const props = withDefaults(defineProps<{
  modelValue: CategorieFormValue;
  loading?: boolean;
  errorMessage?: string;
  submitLabel?: string;
  submitLoadingLabel?: string;
}>(), {
  loading: false,
  errorMessage: "",
  submitLabel: "Enregistrer",
  submitLoadingLabel: "Enregistrement...",
});

const emit = defineEmits<{
  (e: "submit", value: CategorieFormValue): void;
}>();

const localValue = reactive<CategorieFormValue>({
  nomCategorie: "",
});

watch(
  () => props.modelValue,
  (value) => {
    localValue.nomCategorie = value.nomCategorie ?? "";
  },
  { immediate: true, deep: true }
);

const canSubmit = computed(() => {
  return !!localValue.nomCategorie.trim();
});

function onSubmit() {
  emit("submit", {
    nomCategorie: localValue.nomCategorie.trim(),
  });
}
</script>
