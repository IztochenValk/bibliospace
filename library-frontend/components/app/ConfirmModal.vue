<template>
  <!--
    Modal de confirmation generique : titre + message + 2 boutons
    (Annuler / Confirmer). Utilise pour les actions destructives
    (suppression utilisateur, livre, etc.) ou irreversibles.

    Construit sur AppModal pour la coherence d'API et de visuel.

    Usage :
      <ConfirmModal
        ref="confirmRef"
        title="Supprimer cet utilisateur ?"
        message="Cette action est irreversible."
        variant="danger"
        :loading="deleting"
        @confirm="onConfirm"
        @cancel="onCancel"
      />
      confirmRef.value?.open()
  -->
  <AppModal ref="modalRef" :title="title" size="sm" @close="cancel">
    <p class="text-sm text-base-content/80 whitespace-pre-line">
      {{ message }}
    </p>

    <template #actions>
      <button
        type="button"
        class="btn btn-ghost"
        :disabled="loading"
        @click="cancel"
      >
        {{ cancelLabel }}
      </button>

      <button
        type="button"
        class="btn"
        :class="confirmVariantClass"
        :disabled="loading"
        @click="confirm"
      >
        {{ loading ? "..." : confirmLabel }}
      </button>
    </template>
  </AppModal>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import AppModal from "~/components/app/AppModal.vue";

type Variant = "danger" | "warning" | "primary";

const props = withDefaults(defineProps<{
  title?: string;
  message?: string;
  confirmLabel?: string;
  cancelLabel?: string;
  variant?: Variant;
  loading?: boolean;
}>(), {
  title: "Confirmer l'action",
  message: "Cette action est irreversible. Continuer ?",
  confirmLabel: "Confirmer",
  cancelLabel: "Annuler",
  variant: "primary",
  loading: false,
});

const emit = defineEmits<{
  (e: "confirm"): void;
  (e: "cancel"): void;
}>();

const modalRef = ref<InstanceType<typeof AppModal> | null>(null);

const confirmVariantClass = computed(() => {
  if (props.variant === "danger") return "btn-error";
  if (props.variant === "warning") return "btn-warning";
  return "btn-primary";
});

function open(): void {
  modalRef.value?.open();
}

function close(): void {
  modalRef.value?.close();
}

function confirm(): void {
  emit("confirm");
}

function cancel(): void {
  emit("cancel");
  close();
}

defineExpose({
  open,
  close,
});
</script>
