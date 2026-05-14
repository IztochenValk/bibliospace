<template>
  <dialog ref="dialogRef" class="modal">
    <div class="modal-box max-w-lg">
      <h3 class="text-lg font-bold">
        Confirmer le retour
      </h3>

      <p class="mt-3 text-sm text-base-content/70">
        Tu es sur le point de marquer comme retourné :
      </p>

      <div class="mt-3 rounded-lg border border-base-300 bg-base-200/40 p-4">
        <div class="font-medium">
          {{ item?.titreLivre ?? "Sans titre" }}
        </div>
        <div class="mt-1 text-sm text-base-content/60">
          Emprunt #{{ item?.id ?? "-" }}
        </div>
        <div class="mt-1 text-sm text-base-content/60">
          Date d'emprunt : {{ item?.dateEmprunt ?? "-" }}
        </div>
        <div class="mt-1 text-sm text-base-content/60">
          Retour prévu : {{ item?.dateRetourPrevue ?? "-" }}
        </div>
      </div>

      <label class="form-control w-full mt-4">
        <div class="label">
          <span class="label-text">Date de retour effective</span>
        </div>
        <input
          v-model="dateRetourEffective"
          type="date"
          class="input input-bordered w-full"
          :max="maxDate"
          :disabled="loading"
        />
      </label>

      <div v-if="errorMessage" class="alert alert-error mt-4">
        <span>{{ errorMessage }}</span>
      </div>

      <div class="modal-action">
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
          {{ loading ? "Validation..." : "Confirmer le retour" }}
        </button>
      </div>
    </div>

    <form method="dialog" class="modal-backdrop">
      <button @click="close">close</button>
    </form>
  </dialog>
</template>

<script setup lang="ts">
import type { EmpruntResponse } from "~/types/emprunts";

const props = defineProps<{
  item: EmpruntResponse | null;
  loading: boolean;
  errorMessage?: string;
}>();

const emit = defineEmits<{
  (e: "submit", payload: { id: number; dateRetourEffective: string }): void;
}>();

const dialogRef = ref<HTMLDialogElement | null>(null);
const dateRetourEffective = ref("");

const maxDate = computed(() => {
  const d = new Date();
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
});

const canSubmit = computed(() => {
  return !!props.item?.id && !!dateRetourEffective.value;
});

function open() {
  if (!dateRetourEffective.value) {
    dateRetourEffective.value = maxDate.value;
  }
  dialogRef.value?.showModal();
}

function close() {
  dialogRef.value?.close();
}

function submit() {
  if (!props.item?.id || !dateRetourEffective.value) {
    return;
  }

  emit("submit", {
    id: props.item.id,
    dateRetourEffective: dateRetourEffective.value,
  });
}

defineExpose({
  open,
  close,
});
</script>
