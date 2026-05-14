<template>
  <div class="card bg-base-100 border border-base-300">
    <div class="card-body space-y-4">
      <div class="flex justify-end">
        <button class="btn btn-ghost" type="button" @click="$emit('refresh')">
          Rafraîchir
        </button>
      </div>

      <div v-if="loading" class="text-base-content/60">
        Chargement...
      </div>

      <div v-else-if="items.length === 0" class="text-base-content/60">
        Aucun utilisateur.
      </div>

      <div v-else class="overflow-x-auto">
        <table class="table">
          <thead>
            <tr>
              <th>Nom</th>
              <th>Email</th>
              <th>Rôle</th>
              <th>Statut</th>
              <th class="text-right">Actions</th>
            </tr>
          </thead>

          <tbody>
            <tr v-for="user in items" :key="user.id">
              <td>
                <div class="font-medium">{{ user.prenom }} {{ user.nom }}</div>
                <div class="text-xs text-base-content/60">#{{ user.id }}</div>
              </td>

              <td>{{ user.email }}</td>

              <td>
                <UtilisateurRoleBadge :role="user.role" />
              </td>

              <td>
                <span class="badge" :class="getStatusBadgeClass(user.statut)">
                  {{ getStatusLabel(user.statut) }}
                </span>
              </td>

              <td class="text-right">
                <div class="flex justify-end gap-2 flex-wrap">
                  <NuxtLink
                    class="btn btn-sm btn-ghost"
                    :to="`/utilisateurs/${user.id}`"
                  >
                    Voir
                  </NuxtLink>

                  <NuxtLink
                    v-if="canEdit(user)"
                    class="btn btn-sm btn-primary"
                    :to="`/utilisateurs/${user.id}/edit`"
                  >
                    Modifier
                  </NuxtLink>

                  <button
                    v-if="canDelete(user)"
                    class="btn btn-warning btn-sm"
                    type="button"
                    :disabled="actionLoadingId === user.id"
                    @click="$emit('delete', user)"
                  >
                    {{ actionLoadingId === user.id ? "..." : "Désactiver" }}
                  </button>

                  <button
                    v-if="canReactivate(user)"
                    class="btn btn-success btn-sm"
                    type="button"
                    :disabled="actionLoadingId === user.id"
                    @click="$emit('reactivate', user)"
                  >
                    {{ actionLoadingId === user.id ? "..." : "Réactiver" }}
                  </button>

                  <button
                    v-if="canAnonymize(user)"
                    class="btn btn-error btn-sm"
                    type="button"
                    :disabled="actionLoadingId === user.id"
                    @click="$emit('anonymize', user)"
                  >
                    {{ actionLoadingId === user.id ? "..." : "Anonymiser" }}
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import UtilisateurRoleBadge from "~/components/utilisateurs/UtilisateurRoleBadge.vue";
import type { UtilisateurResponse } from "~/types/utilisateurs";

defineProps<{
  items: UtilisateurResponse[];
  loading: boolean;
  actionLoadingId: number | null;
  canDelete: (user: UtilisateurResponse) => boolean;
  canReactivate: (user: UtilisateurResponse) => boolean;
  canAnonymize: (user: UtilisateurResponse) => boolean;
  canEdit: (user: UtilisateurResponse) => boolean;
}>();

defineEmits<{
  (e: "refresh"): void;
  (e: "delete", user: UtilisateurResponse): void;
  (e: "reactivate", user: UtilisateurResponse): void;
  (e: "anonymize", user: UtilisateurResponse): void;
}>();

function getStatusLabel(statut: UtilisateurResponse["statut"]) {
  switch (statut) {
    case "ACTIF":
      return "Actif";
    case "INACTIF":
      return "Désactivé";
    case "ANONYMISE":
      return "Anonymisé";
    default:
      return "Inconnu";
  }
}

function getStatusBadgeClass(statut: UtilisateurResponse["statut"]) {
  switch (statut) {
    case "ACTIF":
      return "badge-success";
    case "INACTIF":
      return "badge-warning";
    case "ANONYMISE":
      return "badge-error";
    default:
      return "badge-ghost";
  }
}
</script>
