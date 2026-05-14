export const StatutEmprunt = {
  EN_COURS: "EN_COURS",
  RETOURNE: "RETOURNE",
} as const;

export type StatutEmprunt = typeof StatutEmprunt[keyof typeof StatutEmprunt];
