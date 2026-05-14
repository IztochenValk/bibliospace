import type { Role } from "~/types/shared";
import { Role as Roles } from "~/types/shared";

export function isAuthenticated(role: Role | null | undefined): boolean {
  return !!role;
}

export function isAdherent(role: Role | null | undefined): boolean {
  return role === Roles.ADHERENT;
}

export function isBibliothecaire(role: Role | null | undefined): boolean {
  return role === Roles.BIBLIOTHECAIRE;
}

export function isAdministrateur(role: Role | null | undefined): boolean {
  return role === Roles.ADMINISTRATEUR;
}

export function isStaff(role: Role | null | undefined): boolean {
  return isBibliothecaire(role) || isAdministrateur(role);
}

export function canBorrowBooks(role: Role | null | undefined): boolean {
  return isAdherent(role);
}

export function canSeeCatalogue(role: Role | null | undefined): boolean {
  return isAdherent(role);
}

export function canAccessMe(role: Role | null | undefined): boolean {
  return isAuthenticated(role);
}

export function canManageEmprunts(role: Role | null | undefined): boolean {
  // "Manage" = vue d'ensemble des emprunts de TOUS les utilisateurs.
  // Les adherents accedent a leurs propres emprunts via "Mes emprunts"
  // (dropdown dedie dans la topbar), pas via cette permission.
  return isStaff(role);
}

export function canManageLivres(role: Role | null | undefined): boolean {
  return isStaff(role);
}

export function canManageUtilisateurs(role: Role | null | undefined): boolean {
  return isStaff(role);
}

export function canAccessBibliothecaireArea(role: Role | null | undefined): boolean {
  return isStaff(role);
}

export function canAccessAdminArea(role: Role | null | undefined): boolean {
  return isAdministrateur(role);
}
