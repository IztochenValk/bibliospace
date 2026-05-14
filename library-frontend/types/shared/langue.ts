/**
 * Miroir TypeScript de l'enum Java com.chague.bibliotheque.domain.Langue.
 *
 * Le backend valide @NotNull + valeur dans { FR, EN, ES } via Jackson.
 * Tout autre valeur (ex: "BG") est rejetee en 400 Bad Request avant
 * meme d'atteindre le controller (HttpMessageNotReadableException).
 */
export const Langue = {
  FR: "FR",
  EN: "EN",
  ES: "ES",
} as const;

export type Langue = typeof Langue[keyof typeof Langue];
