<template>
  <div class="card bg-base-100 border border-base-300">
    <div class="card-body">
      <div class="grid gap-4 md:grid-cols-3">
        <label class="form-control w-full">
          <div class="label">
            <span class="label-text">Statut</span>
          </div>

          <select
            :value="statut"
            class="select select-bordered w-full"
            @change="$emit('update:statut', ($event.target as HTMLSelectElement).value)"
          >
            <option value="TOUS">TOUS</option>
            <option value="EN_COURS">EN_COURS</option>
            <option value="RETOURNE">RETOURNE</option>
          </select>
        </label>

        <label class="form-control w-full">
          <div class="label">
            <span class="label-text">Recherche titre / utilisateur</span>
          </div>

          <input
            :value="search"
            type="text"
            class="input input-bordered w-full"
            placeholder="Rechercher..."
            @input="$emit('update:search', ($event.target as HTMLInputElement).value)"
          />
        </label>

        <label class="form-control w-full">
          <div class="label">
            <span class="label-text">Tri titre</span>
          </div>

          <select
            :value="sortOrder"
            class="select select-bordered w-full"
            @change="$emit('update:sort-order', ($event.target as HTMLSelectElement).value)"
          >
            <option value="ASC">A à Z</option>
            <option value="DESC">Z à A</option>
          </select>
        </label>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  statut: string;
  search: string;
  sortOrder: string;
}>();

defineEmits<{
  (e: "update:statut", value: string): void;
  (e: "update:search", value: string): void;
  (e: "update:sort-order", value: string): void;
}>();
</script>
