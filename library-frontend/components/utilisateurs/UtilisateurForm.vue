<template>
  <form class="space-y-6" @submit.prevent="onSubmit">
    <div class="grid gap-4 md:grid-cols-2">
      <label class="form-control w-full">
        <div class="label">
          <span class="label-text">Nom</span>
        </div>
        <input
          v-model="localValue.nom"
          type="text"
          class="input input-bordered w-full"
          :disabled="loading"
        />
      </label>

      <label class="form-control w-full">
        <div class="label">
          <span class="label-text">Prénom</span>
        </div>
        <input
          v-model="localValue.prenom"
          type="text"
          class="input input-bordered w-full"
          :disabled="loading"
        />
      </label>

      <label class="form-control w-full md:col-span-2">
        <div class="label">
          <span class="label-text">Email</span>
        </div>
        <input
          v-model="localValue.email"
          type="email"
          class="input input-bordered w-full"
          :disabled="loading"
        />
      </label>

      <label class="form-control w-full md:col-span-2">
        <div class="label">
          <span class="label-text">Rôle</span>
          <span
            v-if="lockRole"
            class="label-text-alt"
          >
            Rôle verrouillé
          </span>
        </div>

        <select
          v-model="localValue.role"
          class="select select-bordered w-full"
          :disabled="loading || lockRole"
        >
          <option
            v-for="roleOption in availableRoles"
            :key="roleOption"
            :value="roleOption"
          >
            {{ roleOption }}
          </option>
        </select>
      </label>
    </div>

    <div
      v-if="errorMessage"
      class="alert alert-error"
    >
      <span>{{ errorMessage }}</span>
    </div>

    <div class="flex justify-end gap-3">
      <NuxtLink class="btn btn-ghost" to="/utilisateurs">
        Annuler
      </NuxtLink>

      <button
        class="btn btn-primary"
        type="submit"
        :disabled="loading"
      >
        {{ loading ? "Enregistrement..." : "Enregistrer" }}
      </button>
    </div>
  </form>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from "vue";
import { Role } from "~/types/shared";
import type { Role as RoleType } from "~/types/shared";

type UtilisateurFormValue = {
  nom: string;
  prenom: string;
  email: string;
  role: RoleType;
};

const props = withDefaults(defineProps<{
  modelValue: UtilisateurFormValue;
  loading?: boolean;
  errorMessage?: string;
  lockRole?: boolean;
  allowedRoles?: RoleType[];
}>(), {
  loading: false,
  errorMessage: "",
  lockRole: false,
  allowedRoles: () => [Role.ADHERENT, Role.BIBLIOTHECAIRE],
});

const emit = defineEmits<{
  (e: "submit", value: UtilisateurFormValue): void;
}>();

const localValue = reactive<UtilisateurFormValue>({
  nom: "",
  prenom: "",
  email: "",
  role: Role.ADHERENT,
});

const availableRoles = computed<RoleType[]>(() => {
  if (props.lockRole) {
    return [localValue.role];
  }

  return props.allowedRoles.length ? props.allowedRoles : [Role.ADHERENT];
});

watch(
  () => props.modelValue,
  (value) => {
    localValue.nom = value.nom ?? "";
    localValue.prenom = value.prenom ?? "";
    localValue.email = value.email ?? "";
    localValue.role = value.role ?? Role.ADHERENT;
  },
  { immediate: true, deep: true }
);

watch(
  () => props.allowedRoles,
  (roles) => {
    if (!roles.includes(localValue.role)) {
      localValue.role = roles[0] ?? Role.ADHERENT;
    }
  },
  { immediate: true, deep: true }
);

function onSubmit() {
  emit("submit", {
    nom: localValue.nom.trim(),
    prenom: localValue.prenom.trim(),
    email: localValue.email.trim(),
    role: localValue.role,
  });
}
</script>
