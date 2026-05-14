/**
 * Role utilisateur — miroir TypeScript de l'enum Java Role.
 *
 * Ce double pattern (const + type) est volontaire et necessaire :
 *  - le const objet permet l'utilisation comme VALEUR au runtime
 *    (Object.values(Role), comparaisons Role.ADHERENT === user.role)
 *  - le type permet l'utilisation comme TYPE au compile-time
 *    (parametres fonction, generics, declarations de propriete)
 *
 * Si on ne garde que `export type Role = "ADHERENT" | ...`, alors
 * Role devient undefined a l'execution et les imports valeurs cassent
 * (cf. services/auth/authSessionService.ts qui fait Object.values).
 */
export const Role = {
  ADHERENT: "ADHERENT",
  BIBLIOTHECAIRE: "BIBLIOTHECAIRE",
  ADMINISTRATEUR: "ADMINISTRATEUR",
} as const;

export type Role = typeof Role[keyof typeof Role];