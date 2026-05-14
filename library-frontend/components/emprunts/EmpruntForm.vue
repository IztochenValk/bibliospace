<template>
  <form class="space-y-6" @submit.prevent="onSubmit">
    <div class="grid gap-4 md:grid-cols-2">
      <label class="form-control w-full">
        <div class="label">
          <span class="label-text">Utilisateur</span>
        </div>

        <select
          v-model="localValue.utilisateurId"
          class="select select-bordered w-full"
          :disabled="loading || usersLoading"
        >
          <option value="">
            {{ usersLoading ? "Chargement des utilisateurs..." : "Sélectionner un utilisateur" }}
          </option>

          <option
            v-for="user in sortedUsers"
            :key="user.id"
            :value="String(user.id)"
          >
            {{ user.prenom }} {{ user.nom }} - {{ user.email }}
          </option>
        </select>
      </label>

      <label class="form-control w-full">
        <div class="label">
          <span class="label-text">Filtrer les livres par auteur</span>
        </div>

        <input
          v-model="authorFilter"
          type="text"
          class="input input-bordered w-full"
          placeholder="Nom de l'auteur"
          :disabled="loading || livresLoading"
        />
      </label>

      <label class="form-control w-full md:col-span-2">
        <div class="label">
          <span class="label-text">Livre</span>
        </div>

        <select
          v-model="localValue.livreId"
          class="select select-bordered w-full"
          :disabled="loading || livresLoading"
        >
          <option value="">
            {{ livresLoading ? "Chargement du catalogue..." : "Sélectionner un livre" }}
          </option>

          <option
            v-for="item in filteredLivres"
            :key="item.livreId"
            :value="String(item.livreId)"
          >
            {{ item.titre ?? "Sans titre" }} - {{ item.auteur ?? "Auteur inconnu" }}
          </option>
        </select>
      </label>

      <label class="form-control w-full md:col-span-2">
        <div class="label">
          <span class="label-text">Date retour prévue</span>
        </div>
        <input
          v-model="localValue.dateRetourPrevue"
          type="date"
          class="input input-bordered w-full"
          :disabled="loading"
        />
      </label>
    </div>

    <div class="flex justify-end">
      <button
        class="btn btn-primary"
        type="submit"
        :disabled="loading || !canSubmit"
      >
        {{ loading ? "Création..." : "Créer l'emprunt" }}
      </button>
    </div>
  </form>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import type { UtilisateurResponse } from "~/types/utilisateurs";
import type { CatalogueItemResponse } from "~/types/catalogue";

type EmpruntFormValue = {
  utilisateurId: string;
  livreId: string;
  dateRetourPrevue: string;
};

const props = defineProps<{
  loading?: boolean;
  usersLoading?: boolean;
  livresLoading?: boolean;
  users: UtilisateurResponse[];
  livres: CatalogueItemResponse[];
}>();

const emit = defineEmits<{
  (e: "submit", value: {
    utilisateurId: number;
    livreId: number;
    dateRetourPrevue: string;
  }): void;
}>();

const localValue = reactive<EmpruntFormValue>({
  utilisateurId: "",
  livreId: "",
  dateRetourPrevue: "",
});

const authorFilter = ref("");

const sortedUsers = computed(() => {
  return [...props.users].sort((a, b) => {
    const left = `${a.prenom} ${a.nom}`.trim().toLowerCase();
    const right = `${b.prenom} ${b.nom}`.trim().toLowerCase();
    return left.localeCompare(right);
  });
});

const filteredLivres = computed(() => {
  const filter = authorFilter.value.trim().toLowerCase();

  const items = [...props.livres].sort((a, b) => {
    const left = String(a.titre ?? "").toLowerCase();
    const right = String(b.titre ?? "").toLowerCase();
    return left.localeCompare(right);
  });

  if (!filter) {
    return items;
  }

  return items.filter((item) =>
    String(item.auteur ?? "").toLowerCase().includes(filter)
  );
});

const canSubmit = computed(() => {
  return !!localValue.utilisateurId
    && !!localValue.livreId
    && !!localValue.dateRetourPrevue;
});

function onSubmit() {
  emit("submit", {
    utilisateurId: Number(localValue.utilisateurId),
    livreId: Number(localValue.livreId),
    dateRetourPrevue: localValue.dateRetourPrevue,
  });
}
</script>
