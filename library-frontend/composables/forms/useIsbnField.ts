import { computed, ref, type Ref } from "vue";
import { useRandomIsbn } from "~/composables/useRandomIsbn";
import { formatIsbn10, isValidIsbn10, normalizeIsbn10 } from "~/utils/isbn";

/**
 * Composable pour le champ ISBN-10 d'un formulaire.
 *
 * Encapsule :
 *  - l'état "touché" (pour ne pas afficher l'erreur avant interaction)
 *  - la validation du format ISBN-10 (9 chiffres + clé)
 *  - le formatage d'aperçu (3-1-1-3-1-1)
 *  - la génération d'un ISBN aléatoire valide (via useRandomIsbn)
 *
 * Le composable mute le ref isbn passé en argument (même pattern que
 * useImageUpload : le parent garde la maîtrise de son état).
 *
 * Usage :
 *
 *   const isbnRef = toRef(localValue, "isbn");
 *   const {
 *     isbnTouched, isbnFieldValid, formattedIsbnPreview, formatExample,
 *     generateIsbn, markTouched,
 *   } = useIsbnField(isbnRef);
 */
export function useIsbnField(isbn: Ref<string>) {
  const { generate } = useRandomIsbn();

  const isbnTouched = ref(false);

  /** Exemple à afficher dans le message d'erreur. */
  const formatExample = formatIsbn10("0306406152");

  /** Un ISBN vide est considéré comme valide (champ optionnel). */
  const isbnFieldValid = computed(() => {
    const raw = isbn.value.trim();
    if (!raw) return true;
    return isValidIsbn10(raw);
  });

  /** Aperçu formaté de l'ISBN saisi, si exactement 10 caractères normalisés. */
  const formattedIsbnPreview = computed(() => {
    const raw = normalizeIsbn10(isbn.value);
    return raw.length === 10 ? formatIsbn10(raw) : "";
  });

  function generateIsbn() {
    isbn.value = generate();
    isbnTouched.value = false;
  }

  /**
   * À câbler sur @blur du champ ISBN. Sans argument : marque comme touché.
   * Avec false : reset (utile après un re-sync depuis le parent).
   */
  function markTouched(value: boolean = true) {
    isbnTouched.value = value;
  }

  return {
    isbnTouched,
    isbnFieldValid,
    formattedIsbnPreview,
    formatExample,
    generateIsbn,
    markTouched,
  };
}
