<template>
  <div class="mx-auto w-full max-w-4xl">
    <div class="card bg-base-100 border border-base-300">
      <div class="card-body">
        <div class="flex flex-col gap-6 md:flex-row">
          <!-- Couverture -->
          <div class="w-full md:w-56 md:flex-shrink-0">
            <div class="overflow-hidden rounded-xl border border-base-300 bg-base-200">
              <img
                v-if="resolvedImageUrl"
                :src="resolvedImageUrl"
                :alt="livre.titre"
                class="h-80 w-full object-cover"
              />
              <div
                v-else
                class="flex h-80 w-full items-center justify-center text-sm text-base-content/40"
              >
                Aucune image
              </div>
            </div>
          </div>

          <!-- Infos livre -->
          <div class="flex-1 space-y-3">
            <!-- Titre + Auteur + Langue empilés -->
            <div>
              <h2 class="text-2xl font-bold">{{ livre.titre }}</h2>
              <p class="text-base-content/70">{{ livre.auteur }}</p>
              <p class="text-sm text-base-content/60">
                {{ livre.langue || "Langue non renseignée" }}
              </p>
            </div>

            <!-- Catégories -->
            <div>
              <div class="text-xs uppercase tracking-wide text-base-content/50">
                Catégories
              </div>
              <div
                v-if="livre.categories && livre.categories.length > 0"
                class="mt-1 flex flex-wrap gap-1.5"
              >
                <span
                  v-for="cat in livre.categories"
                  :key="cat.id"
                  class="badge badge-primary"
                >
                  {{ cat.nomCategorie }}
                </span>
              </div>
              <div
                v-else
                class="text-sm text-base-content/50"
              >
                Aucune catégorie
              </div>
            </div>

            <!-- ID -->
            <div>
              <div class="text-xs uppercase tracking-wide text-base-content/50">
                ID
              </div>
              <div>{{ livre.id }}</div>
            </div>

            <!-- Description -->
            <div>
              <div class="text-xs uppercase tracking-wide text-base-content/50">
                Description
              </div>
              <div class="whitespace-pre-line">
                {{ livre.description || "Aucune description" }}
              </div>
            </div>

            <!-- Image URL -->
            <div>
              <div class="text-xs uppercase tracking-wide text-base-content/50">
                Image URL
              </div>
              <div class="break-all">
                {{ livre.imageUrl || "-" }}
              </div>
            </div>

            <!-- Bouton Modifier (staff uniquement) -->
            <div v-if="isStaff" class="pt-1">
              <NuxtLink
                class="btn btn-primary"
                :to="`/livres/${livre.id}/edit`"
              >
                Modifier
              </NuxtLink>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { LivreResponse } from "~/types/livres";
import { useImageUrl } from "~/composables/utils/useImageUrl";
import { Role } from "~/types/shared";

const props = defineProps<{
  livre: LivreResponse;
}>();

const { resolveImageUrl } = useImageUrl();
const auth = useAuth();

const resolvedImageUrl = computed(() => resolveImageUrl(props.livre.imageUrl));

// Le bouton Modifier n'est affiche qu'aux bibliothecaires et administrateurs.
// Un adherent qui consulte la fiche depuis le catalogue ne le voit pas.
const isStaff = computed(() =>
  auth.role.value === Role.BIBLIOTHECAIRE || auth.role.value === Role.ADMINISTRATEUR
);
</script>
