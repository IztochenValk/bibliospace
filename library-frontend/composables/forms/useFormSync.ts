import { reactive, watch } from "vue";

/**
 * Composable de synchronisation entre un modelValue parent et un reactive
 * local éditable par le composant.
 *
 * Pattern : le parent passe une valeur via la prop modelValue, le composant
 * veut une copie locale qu'il peut muter librement (sans pousser chaque
 * changement vers le parent à la frappe). À la soumission, le composant
 * émet la valeur finale au parent.
 *
 * Le watch est { immediate: true, deep: true } : la première exécution
 * initialise le local avec le parent, et chaque changement de modelValue
 * côté parent (par exemple un fetchLivre(id) qui peuple le formulaire en
 * mode édition) repuse les champs.
 *
 * Le callback onSync, optionnel, est appelé après chaque resync. Utile pour
 * réinitialiser des flags type isbnTouched.
 *
 * Usage :
 *
 *   const localValue = useFormSync(
 *     () => props.modelValue,
 *     {
 *       titre: "", auteur: "", description: "",
 *       langue: "", imageUrl: "", isbn: "", quantiteTotale: 0,
 *     },
 *     () => { isbnTouched.value = false; }
 *   );
 */
export function useFormSync<T extends Record<string, any>>(
  source: () => T,
  defaults: T,
  onSync?: () => void
): T {
  const local = reactive({ ...defaults }) as T;

  watch(
    source,
    (value) => {
      for (const key of Object.keys(defaults) as Array<keyof T>) {
        const v = value?.[key];
        (local as any)[key] = v ?? defaults[key];
      }
      onSync?.();
    },
    { immediate: true, deep: true }
  );

  return local;
}
