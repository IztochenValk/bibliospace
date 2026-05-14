<template>
  <div class="toast-region" aria-live="polite" aria-atomic="false">
    <div
      v-for="item in items"
      :key="item.id"
      class="toast-item"
      :class="toastClass(item.type)"
      :role="item.type === 'error' ? 'alert' : 'status'"
    >
      <div class="toast-message">
        {{ item.message }}
      </div>

      <button
        type="button"
        class="toast-close"
        :aria-label="`Fermer le message: ${item.message}`"
        @click="remove(item.id)"
      >
        Fermer
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useToast } from "~/composables/ui/useToast";

type ToastKind = "success" | "error" | "info" | "warning";

const { items, remove } = useToast();

function toastClass(type: ToastKind) {
  if (type === "success") return "toast-success";
  if (type === "error") return "toast-error";
  if (type === "warning") return "toast-warning";
  return "toast-info";
}
</script>

<style scoped>
.toast-region {
  position: fixed;
  top: 1rem;
  right: 1rem;
  z-index: 60;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  width: min(420px, calc(100vw - 2rem));
}

.toast-item {
  border: 1px solid rgb(71 85 105 / 0.9);
  border-radius: 1rem;
  background: rgb(15 23 42 / 0.96);
  padding: 0.9rem 1rem;
  box-shadow: 0 18px 45px rgba(0, 0, 0, 0.35);
}

.toast-message {
  font-size: 0.95rem;
  line-height: 1.45;
  margin-bottom: 0.75rem;
}

.toast-close {
  border: 1px solid rgb(71 85 105 / 0.9);
  border-radius: 0.7rem;
  padding: 0.45rem 0.7rem;
  background: transparent;
  cursor: pointer;
}

.toast-success {
  border-left: 4px solid currentColor;
}

.toast-error {
  border-left: 4px solid currentColor;
}

.toast-warning {
  border-left: 4px solid currentColor;
}

.toast-info {
  border-left: 4px solid currentColor;
}
</style>
