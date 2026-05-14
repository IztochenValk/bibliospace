export const StatutUtilisateur = {
  ACTIF: "ACTIF",
  INACTIF: "INACTIF",
  ANONYMISE: "ANONYMISE",
} as const;

export type StatutUtilisateur = typeof StatutUtilisateur[keyof typeof StatutUtilisateur];
