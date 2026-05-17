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
          :class="{ 'input-error': dateError }"
          :min="minDate"
          :disabled="loading"
        />
        <div v-if="dateError" class="label">
          <span class="label-text-alt text-error">{{ dateError }}</span>
        </div>
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
const dateError = ref("");

// Date du jour au format YYYY-MM-DD, utilisée comme borne inférieure du
// champ date. Validation client (étape 4 du diagramme de séquence) : le
// navigateur empêche la sélection d'une date antérieure à aujourd'hui via
// l'attribut min, et un garde-fou JS explicite refuse la soumission en
// double sécurité au cas où l'attribut min serait contourné.
const minDate = computed(() => {
  const d = new Date();
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
});

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
  // Validation client de la date — étape 4 du diagramme de séquence :
  // on bloque la soumission si la date est antérieure à aujourd'hui,
  // pour éviter l'aller-retour HTTP et donner un feedback immédiat.
  // La validation côté serveur (@FutureOrPresent + GlobalControllerAdvice)
  // reste la source de vérité, conformément au pattern défense-en-profondeur.
  dateError.value = "";
  if (localValue.dateRetourPrevue && localValue.dateRetourPrevue < minDate.value) {
    dateError.value = "La date de retour ne peut pas être antérieure à aujourd'hui";
    return;
  }

  emit("submit", {
    utilisateurId: Number(localValue.utilisateurId),
    livreId: Number(localValue.livreId),
    dateRetourPrevue: localValue.dateRetourPrevue,
  });
}
</script>
