<template>
  <div class="mt-6 flex flex-col gap-4 rounded-xl border border-base-300 bg-base-100 p-4 md:flex-row md:items-center md:justify-between">
    <div class="flex items-center gap-3">
      <span class="text-sm text-base-content/70">Éléments par page</span>

      <select
        class="select select-bordered select-sm w-24"
        :value="pageSize"
        @change="onPageSizeChange"
      >
        <option
          v-for="option in pageSizeOptions"
          :key="option"
          :value="option"
        >
          {{ option }}
        </option>
      </select>

      <span class="text-sm text-base-content/70">
        {{ startItem }} à {{ endItem }} sur {{ totalItems }}
      </span>
    </div>

    <div class="flex flex-wrap items-center gap-2">
      <button
        class="btn btn-sm btn-ghost"
        type="button"
        :disabled="currentPage <= 1"
        @click="$emit('change-page', currentPage - 1)"
      >
        Précédent
      </button>

      <button
        v-for="page in visiblePages"
        :key="page"
        class="btn btn-sm"
        :class="page === currentPage ? 'btn-primary' : 'btn-ghost'"
        type="button"
        @click="$emit('change-page', page)"
      >
        {{ page }}
      </button>

      <button
        class="btn btn-sm btn-ghost"
        type="button"
        :disabled="currentPage >= totalPages"
        @click="$emit('change-page', currentPage + 1)"
      >
        Suivant
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = withDefaults(defineProps<{
  currentPage: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  pageSizeOptions?: number[];
}>(), {
  pageSizeOptions: () => [10, 15, 20],
});

const emit = defineEmits<{
  (e: "change-page", page: number): void;
  (e: "change-page-size", pageSize: number): void;
}>();

const startItem = computed(() => {
  if (props.totalItems === 0) return 0;
  return (props.currentPage - 1) * props.pageSize + 1;
});

const endItem = computed(() => {
  if (props.totalItems === 0) return 0;
  return Math.min(props.currentPage * props.pageSize, props.totalItems);
});

const visiblePages = computed(() => {
  const total = props.totalPages;
  const current = props.currentPage;

  if (total <= 1) {
    return [1];
  }

  const pages: number[] = [];
  const start = Math.max(1, current - 2);
  const end = Math.min(total, current + 2);

  for (let i = start; i <= end; i += 1) {
    pages.push(i);
  }

  return pages;
});

function onPageSizeChange(event: Event) {
  const target = event.target as HTMLSelectElement;
  emit("change-page-size", Number(target.value));
}
</script>
