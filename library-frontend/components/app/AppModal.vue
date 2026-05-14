<template>
  <!--
    Composant de base pour TOUS les modaux du projet.

    Encapsule l'element HTML5 <dialog> natif (acessibilite gere par le
    navigateur : focus trap, Escape pour fermer, etc.) avec la stack
    visuelle DaisyUI (modal-box / modal-backdrop / modal-action).

    API impérative :
      <AppModal ref="modalRef" title="Confirmer">
        <p>Contenu</p>
        <template #actions>...</template>
      </AppModal>
      modalRef.value?.open()
      modalRef.value?.close()

    Slots disponibles :
      - default : corps du modal
      - actions : footer (boutons)
      - title   : alternative au prop title si on veut du markup riche
  -->
  <dialog
    ref="dialogRef"
    class="modal"
    @close="onClose"
  >
    <div class="modal-box" :class="sizeClass">
      <header v-if="hasTitle" class="mb-2">
        <slot name="title">
          <h3 class="text-lg font-bold">{{ title }}</h3>
        </slot>
      </header>

      <div class="space-y-3">
        <slot />
      </div>

      <footer v-if="$slots.actions" class="modal-action">
        <slot name="actions" />
      </footer>
    </div>

    <!-- Backdrop : un form method="dialog" permet de fermer le modal en
         cliquant en dehors, en respectant l'API native <dialog>. -->
    <form method="dialog" class="modal-backdrop">
      <button type="button" aria-label="Fermer">close</button>
    </form>
  </dialog>
</template>

<script setup lang="ts">
import { computed, ref, useSlots } from "vue";

type Size = "sm" | "md" | "lg" | "xl";

const props = withDefaults(defineProps<{
  /** Titre affiche dans le header. Optionnel si on utilise le slot #title. */
  title?: string;
  /** Largeur max du modal (defaut: md). */
  size?: Size;
}>(), {
  size: "md",
});

const emit = defineEmits<{
  /** Emis quand l'utilisateur ferme via Escape ou clic backdrop. */
  (e: "close"): void;
}>();

const slots = useSlots();
const dialogRef = ref<HTMLDialogElement | null>(null);

const hasTitle = computed(() => !!props.title || !!slots.title);

const sizeClass = computed(() => {
  switch (props.size) {
    case "sm": return "max-w-sm";
    case "lg": return "max-w-lg";
    case "xl": return "max-w-2xl";
    default:   return "max-w-md";
  }
});

function open(): void {
  dialogRef.value?.showModal();
}

function close(): void {
  dialogRef.value?.close();
}

function onClose(): void {
  // Le navigateur emit "close" sur le <dialog> quand l'utilisateur appuie
  // sur Escape OU clique sur le bouton du backdrop. On propage l'event
  // au parent pour qu'il puisse reset son state si necessaire.
  emit("close");
}

defineExpose({
  open,
  close,
});
</script>
