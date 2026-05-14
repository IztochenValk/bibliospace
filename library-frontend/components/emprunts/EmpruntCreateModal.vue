<template>
  <!--
    Modal de creation d'un emprunt. Affiche les infos du livre selectionne
    et demande la date de retour prevue.

    Construit sur AppModal pour heriter du <dialog> natif et de l'API
    impérative open()/close().
  -->
  <AppModal
    ref="modalRef"
    title="Emprunter un livre"
    size="lg"
  >
    <p class="text-sm text-base-content/70">
      <span class="font-medium">{{ item?.titre ?? "Sans titre" }}</span>
      <span v-if="item?.auteur"> par {{ item.auteur }}</span>
    </p>

    <label class="form-control w-full">
      <div class="label">
        <span class="label-text">Date de retour prévue</span>
      </div>
      <input
        v-model="dateRetourPrevue"
        type="date"
        class="input input-bordered w-full"
        :min="minDate"
        :disabled="loading"
      />
    </label>

    <div v-if="errorMessage" class="alert alert-error">
      <span>{{ errorMessage }}</span>
    </div>

    <template #actions>
      <button
        type="button"
        class="btn btn-ghost"
        :disabled="loading"
        @click="close"
      >
        Annuler
      </button>

      <button
        type="button"
        class="btn btn-primary"
        :disabled="loading || !canSubmit"
        @click="submit"
      >
        {{ loading ? "Emprunt..." : "Confirmer" }}
      </button>
    </template>
  </AppModal>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import AppModal from "~/components/app/AppModal.vue";
import type { CatalogueItemResponse } from "~/types/catalogue";

const props = defineProps<{
  item: CatalogueItemResponse | null;
  loading: boolean;
  errorMessage?: string;
}>();

const emit = defineEmits<{
  (e: "submit", payload: { livreId: number; dateRetourPrevue: string }): void;
}>();

const modalRef = ref<InstanceType<typeof AppModal> | null>(null);
const dateRetourPrevue = ref("");

const minDate = computed(() => {
  const d = new Date();
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
});

const canSubmit = computed(() => {
  return !!props.item?.livreId && !!dateRetourPrevue.value;
});

function open(): void {
  // Pre-remplit la date du jour si vide. Le user peut ensuite la modifier.
  if (!dateRetourPrevue.value) {
    dateRetourPrevue.value = minDate.value;
  }
  modalRef.value?.open();
}

function close(): void {
  modalRef.value?.close();
}

function submit(): void {
  if (!props.item?.livreId || !dateRetourPrevue.value) {
    return;
  }

  emit("submit", {
    livreId: props.item.livreId,
    dateRetourPrevue: dateRetourPrevue.value,
  });
}

defineExpose({
  open,
  close,
});
</script>
