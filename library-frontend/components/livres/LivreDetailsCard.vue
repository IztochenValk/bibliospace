<template>
  <div class="card bg-base-100 border border-base-300">
    <div class="card-body space-y-6">
      <div class="flex flex-col gap-6 md:flex-row">
        <div class="w-full md:w-56">
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

        <div class="flex-1 space-y-4">
          <div>
            <h2 class="text-2xl font-bold">{{ livre.titre }}</h2>
            <p class="text-base-content/70">{{ livre.auteur }}</p>
          </div>

          <div class="grid gap-4 md:grid-cols-2">
            <div>
              <div class="text-xs uppercase tracking-wide text-base-content/50">
                ID
              </div>
              <div class="mt-1">{{ livre.id }}</div>
            </div>

            <div>
              <div class="text-xs uppercase tracking-wide text-base-content/50">
                Langue
              </div>
              <div class="mt-1">{{ livre.langue || "-" }}</div>
            </div>

            <div class="md:col-span-2">
              <div class="text-xs uppercase tracking-wide text-base-content/50">
                Description
              </div>
              <div class="mt-1 whitespace-pre-line">
                {{ livre.description || "Aucune description" }}
              </div>
            </div>

            <div class="md:col-span-2">
              <div class="text-xs uppercase tracking-wide text-base-content/50">
                Image URL
              </div>
              <div class="mt-1 break-all">
                {{ livre.imageUrl || "-" }}
              </div>
            </div>
          </div>

          <div v-if="isStaff" class="pt-2">
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
