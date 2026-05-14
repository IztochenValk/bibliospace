<template>
  <div class="card bg-base-100 border border-base-300">
    <div class="card-body space-y-4">
      <div class="flex items-start justify-between gap-4">
        <div>
          <h2 class="card-title">
            {{ utilisateur.prenom }} {{ utilisateur.nom }}
          </h2>
          <p class="text-sm text-base-content/60">
            Utilisateur #{{ utilisateur.id }}
          </p>
        </div>

        <UtilisateurRoleBadge :role="utilisateur.role" />
      </div>

      <div class="grid gap-4 md:grid-cols-2">
        <div>
          <div class="text-xs uppercase tracking-wide text-base-content/50">
            Email
          </div>
          <div class="mt-1">{{ utilisateur.email }}</div>
        </div>

        <div>
          <div class="text-xs uppercase tracking-wide text-base-content/50">
            Rôle
          </div>
          <div class="mt-1">{{ utilisateur.role }}</div>
        </div>

        <div>
          <div class="text-xs uppercase tracking-wide text-base-content/50">
            Statut
          </div>
          <div class="mt-1">
            {{ getStatusLabel(utilisateur.statut) }}
          </div>
        </div>
      </div>

      <div class="pt-2">
        <NuxtLink
          class="btn btn-primary"
          :to="`/utilisateurs/${utilisateur.id}/edit`"
        >
          Modifier
        </NuxtLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import UtilisateurRoleBadge from "~/components/utilisateurs/UtilisateurRoleBadge.vue";
import type { UtilisateurResponse } from "~/types/utilisateurs";

defineProps<{
  utilisateur: UtilisateurResponse;
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
</script>
