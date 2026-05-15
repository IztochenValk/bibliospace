<template>
  <form class="space-y-6" @submit.prevent="onSubmit">
    <div class="grid grid-cols-1 gap-6 md:grid-cols-12">
      <!-- COLONNE GAUCHE : Jaquette (4/12) -->
      <div class="md:col-span-4">
        <div class="card h-full border border-base-300 bg-base-100">
          <div class="card-body space-y-3">
            <div>
              <h3 class="text-base font-semibold">
                Jaquette
                <span class="text-xs font-normal text-base-content/50">(optionnel)</span>
              </h3>
            </div>

            <div class="overflow-hidden rounded-xl border border-base-300 bg-base-200">
              <div class="flex min-h-[300px] items-center justify-center p-3">
                <img
                  v-if="previewImageUrl"
                  :src="resolvedPreviewUrl"
                  alt="Aperçu du livre"
                  class="max-h-[280px] w-auto max-w-full rounded-lg object-cover shadow-lg"
                />
                <div
                  v-else
                  class="flex h-[260px] w-full items-center justify-center rounded-lg border border-dashed border-base-300 text-xs text-base-content/40"
                >
                  Aucune image sélectionnée
                </div>
              </div>
            </div>

            <div class="space-y-2">
              <!-- File input + bouton upload sur la même ligne -->
              <div class="flex flex-wrap gap-2 items-stretch">
                <input
                  ref="fileInputRef"
                  type="file"
                  accept="image/png,image/jpeg,image/webp,image/gif"
                  class="file-input file-input-bordered file-input-sm flex-1 min-w-[180px]"
                  :disabled="loading || uploadLoading"
                  @change="onFileChange"
                />

                <button
                  class="btn btn-primary btn-sm whitespace-nowrap"
                  type="button"
                  :disabled="loading || uploadLoading || !selectedFile"
                  @click="uploadImage"
                >
                  {{ uploadLoading ? "Upload..." : "Uploader" }}
                </button>
              </div>

              <button
                v-if="localValue.imageUrl"
                class="btn btn-ghost btn-sm"
                type="button"
                :disabled="loading || uploadLoading"
                @click="clearImage"
              >
                Retirer l'image
              </button>

              <div
                v-if="uploadErrorMessage"
                class="alert alert-error"
              >
                <span>{{ uploadErrorMessage }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- COLONNE DROITE : Champs textes (8/12) -->
      <div class="md:col-span-8">
        <div class="card border border-base-300 bg-base-100">
          <div class="card-body space-y-3">
            <!-- Ligne 1 : Titre (7/12) + Auteur (5/12) -->
            <div class="grid gap-4 md:grid-cols-12">
              <label class="form-control md:col-span-7">
                <div class="label">
                  <span class="label-text">
                    Titre <span class="text-error">*</span>
                  </span>
                </div>
                <input
                  v-model="localValue.titre"
                  type="text"
                  class="input input-bordered w-full"
                  :disabled="loading"
                />
              </label>

              <label class="form-control md:col-span-5">
                <div class="label">
                  <span class="label-text">
                    Auteur <span class="text-error">*</span>
                  </span>
                </div>
                <input
                  v-model="localValue.auteur"
                  type="text"
                  class="input input-bordered w-full"
                  :disabled="loading"
                />
              </label>
            </div>

            <!-- Ligne 2 : Quantité (2/12) + Langue (2/12) + ISBN (8/12) -->
            <div class="grid gap-4 md:grid-cols-12">
              <label class="form-control md:col-span-2">
                <div class="label">
                  <span class="label-text">
                    Qté <span class="text-error">*</span>
                  </span>
                </div>
                <input
                  v-model.number="localValue.quantiteTotale"
                  type="number"
                  min="0"
                  class="input input-bordered w-full"
                  :disabled="loading"
                />
              </label>

              <label class="form-control md:col-span-2">
                <div class="label">
                  <span class="label-text">
                    Langue <span class="text-error">*</span>
                  </span>
                </div>
                <select
                  v-model="localValue.langue"
                  class="select select-bordered w-full"
                  :disabled="loading"
                >
                  <option value="">—</option>
                  <option
                    v-for="option in languageOptions"
                    :key="option.value"
                    :value="option.value"
                  >
                    {{ option.label }}
                  </option>
                </select>
              </label>

              <label class="form-control md:col-span-8">
                <div class="label">
                  <span class="label-text">
                    ISBN-10
                    <span class="text-xs font-normal text-base-content/50">(optionnel)</span>
                  </span>
                  <button
                    class="link link-primary text-xs"
                    type="button"
                    :disabled="loading"
                    @click="generateIsbn"
                  >
                    Générer un ISBN valide
                  </button>
                </div>
                <input
                  v-model="localValue.isbn"
                  type="text"
                  class="input input-bordered w-full font-mono tracking-wider"
                  :class="{ 'input-error': isbnTouched && !isbnFieldValid }"
                  placeholder="0123456789"
                  maxlength="13"
                  :disabled="loading"
                  @blur="markTouched"
                />
                <div class="label">
                  <span class="label-text-alt">
                    <template v-if="!localValue.isbn.trim()">
                      <span class="text-base-content/50">9 chiffres + clé de contrôle (ou laisser vide)</span>
                    </template>
                    <template v-else-if="isbnFieldValid">
                      <span class="text-success">
                        ✓ Valide — {{ formattedIsbnPreview }}
                      </span>
                    </template>
                    <template v-else>
                      <span class="text-error">
                        ✗ Format attendu : 9 chiffres + clé (ex : {{ formatExample }})
                      </span>
                    </template>
                  </span>
                </div>
              </label>
            </div>

            <!-- Description (full width) -->
            <label class="form-control w-full">
              <div class="label">
                <span class="label-text">
                  Description
                  <span class="text-xs font-normal text-base-content/50">(optionnel)</span>
                </span>
              </div>
              <textarea
                v-model="localValue.description"
                class="textarea textarea-bordered min-h-20 w-full"
                maxlength="1000"
                :disabled="loading"
              />
              <div class="label">
                <span class="label-text-alt text-base-content/50">
                  {{ localValue.description.length }} / 1000 caractères
                </span>
              </div>
            </label>

            <!-- Catégories (multi-sélection) -->
            <div class="form-control w-full">
              <div class="label">
                <span class="label-text">
                  Catégories
                  <span class="text-xs font-normal text-base-content/50">
                    (optionnel — cliquez pour cocher / décocher)
                  </span>
                </span>
                <span class="label-text-alt text-base-content/50">
                  {{ localValue.categorieIds.length }} sélectionnée(s)
                </span>
              </div>

              <div
                v-if="categoriesLoading"
                class="text-sm text-base-content/50"
              >
                Chargement des catégories...
              </div>

              <div
                v-else-if="categoriesError"
                class="alert alert-error"
              >
                <span>{{ categoriesError }}</span>
              </div>

              <div
                v-else-if="categoriesList.length === 0"
                class="text-sm text-base-content/50"
              >
                Aucune catégorie disponible — créez-en d'abord depuis la page
                <NuxtLink class="link link-primary" to="/categories">Catégories</NuxtLink>.
              </div>

              <div v-else class="flex flex-wrap gap-2 pt-1">
                <button
                  v-for="cat in categoriesList"
                  :key="cat.id"
                  type="button"
                  class="badge badge-lg cursor-pointer transition-colors"
                  :class="
                    isCategorieSelected(cat.id)
                      ? 'badge-primary'
                      : 'badge-outline hover:badge-primary'
                  "
                  :disabled="loading"
                  @click="toggleCategorie(cat.id)"
                >
                  {{ cat.nomCategorie }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="errorMessage"
      class="alert alert-error"
    >
      <span>{{ errorMessage }}</span>
    </div>

    <div class="flex justify-end gap-3">
      <NuxtLink class="btn btn-ghost" to="/livres">
        Annuler
      </NuxtLink>

      <button
        class="btn btn-primary"
        type="submit"
        :disabled="loading || uploadLoading"
      >
        {{ loading ? "Enregistrement..." : submitLabel }}
      </button>
    </div>
  </form>
</template>

<script setup lang="ts">
import { onMounted, ref, toRef, watch } from "vue";
import { useFormSync } from "~/composables/forms/useFormSync";
import { useImageUpload } from "~/composables/forms/useImageUpload";
import { useIsbnField } from "~/composables/forms/useIsbnField";
import { emptyLivreFormValue, type LivreFormValue } from "~/types/livres";
import { listCategories } from "~/services/categoriesService";
import type { Categorie } from "~/types/categories";

const props = withDefaults(defineProps<{
  modelValue: LivreFormValue;
  loading?: boolean;
  errorMessage?: string;
  submitLabel?: string;
}>(), {
  loading: false,
  errorMessage: "",
  submitLabel: "Enregistrer",
});

const emit = defineEmits<{
  (e: "submit", value: LivreFormValue): void;
}>();

const languageOptions = [
  { value: "FR", label: "FR" },
  { value: "EN", label: "EN" },
  { value: "ES", label: "ES" },
] as const;

// État local synchronisé avec le parent (sync à la frappe non remontée).
// Voir composables/forms/useFormSync.ts.
const localValue = useFormSync<LivreFormValue>(
  () => props.modelValue,
  emptyLivreFormValue
);

// Upload d'image : voir composables/forms/useImageUpload.ts.
const {
  fileInputRef,
  selectedFile,
  uploadLoading,
  uploadErrorMessage,
  previewImageUrl,
  resolvedPreviewUrl,
  onFileChange,
  uploadImage,
  clearImage,
} = useImageUpload(toRef(localValue, "imageUrl"));

// Champ ISBN avec validation et génération aléatoire :
// voir composables/forms/useIsbnField.ts.
const {
  isbnTouched,
  isbnFieldValid,
  formattedIsbnPreview,
  formatExample,
  generateIsbn,
  markTouched,
} = useIsbnField(toRef(localValue, "isbn"));

// Reset le flag "touché" à chaque resync depuis le parent (mode édition).
// Ce watch est volontairement séparé de useFormSync pour éviter une TDZ
// (markTouched est déclaré plus bas via destructuring de useIsbnField).
watch(() => props.modelValue, () => markTouched(false));

// ====================================================================
// Sélecteur de catégories
// ====================================================================
// On charge la liste complète des catégories au montage du composant
// pour alimenter le multi-sélecteur. Le state des catégories cochées
// vit dans localValue.categorieIds (tableau d'ids), pré-rempli en mode
// édition via livreToFormValue() côté composable de page.
const categoriesList = ref<Categorie[]>([]);
const categoriesLoading = ref(false);
const categoriesError = ref("");

onMounted(async () => {
  categoriesLoading.value = true;
  categoriesError.value = "";
  try {
    categoriesList.value = await listCategories();
  } catch {
    categoriesError.value = "Impossible de charger les catégories";
  } finally {
    categoriesLoading.value = false;
  }
});

function isCategorieSelected(id: number): boolean {
  return localValue.categorieIds.includes(id);
}

function toggleCategorie(id: number) {
  if (isCategorieSelected(id)) {
    localValue.categorieIds = localValue.categorieIds.filter((x) => x !== id);
  } else {
    localValue.categorieIds = [...localValue.categorieIds, id];
  }
}

function onSubmit() {
  // On émet la valeur brute du formulaire : trim, normalisation ISBN et
  // coercion null/"" sont concentrés dans utils/mappers/livreFormMapper
  // côté page composable. Le composant reste un simple binding UI.
  emit("submit", { ...localValue });
}
</script>
