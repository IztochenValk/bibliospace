import { buildRandomIsbn10 } from "~/utils/isbn";

/**
 * Génère un ISBN-10 valide prêt à l'emploi (10 caractères, clé correcte).
 */
export function useRandomIsbn() {
  function generate(): string {
    return buildRandomIsbn10();
  }
  return { generate };
}
