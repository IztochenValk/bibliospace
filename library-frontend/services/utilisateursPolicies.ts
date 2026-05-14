import type { Utilisateur } from "~/types/utilisateurs/model";
import type { Role as RoleType } from "~/types/shared";
import { Role } from "~/types/shared";

/**
 * Regles de permission UI pour les actions sur les utilisateurs.
 *
 * Pure presentation logic : ce module ne fait AUCUN appel reseau, il
 * decide simplement si tel bouton doit etre cliquable, dans tel etat,
 * pour tel acteur. Le serveur reste l'autorite finale via @PreAuthorize.
 */

export function canDeleteUtilisateur(
  currentRole: RoleType | null | undefined,
  currentUserId: number | null | undefined,
  target: Utilisateur
): boolean {
  if (!currentRole) {
    return false;
  }

  if (target.statut !== "ACTIF") {
    return false;
  }

  if (currentUserId && currentUserId === target.id) {
    return false;
  }

  if (currentRole === Role.ADMINISTRATEUR) {
    return target.role !== Role.ADMINISTRATEUR;
  }

  if (currentRole === Role.BIBLIOTHECAIRE) {
    return target.role === Role.ADHERENT;
  }

  return false;
}

export function canReactivateUtilisateur(
  currentRole: RoleType | null | undefined,
  currentUserId: number | null | undefined,
  target: Utilisateur
): boolean {
  if (!currentRole) {
    return false;
  }

  if (target.statut !== "INACTIF") {
    return false;
  }

  if (currentUserId && currentUserId === target.id) {
    return false;
  }

  if (currentRole === Role.ADMINISTRATEUR) {
    return target.role !== Role.ADMINISTRATEUR;
  }

  if (currentRole === Role.BIBLIOTHECAIRE) {
    return target.role === Role.ADHERENT;
  }

  return false;
}

export function canAnonymizeUtilisateur(
  currentRole: RoleType | null | undefined,
  currentUserId: number | null | undefined,
  target: Utilisateur
): boolean {
  if (!currentRole) {
    return false;
  }

  if (target.statut === "ANONYMISE") {
    return false;
  }

  if (currentUserId && currentUserId === target.id) {
    return false;
  }

  if (currentRole === Role.ADMINISTRATEUR) {
    return target.role !== Role.ADMINISTRATEUR;
  }

  if (currentRole === Role.BIBLIOTHECAIRE) {
    return target.role === Role.ADHERENT;
  }

  return false;
}

export function canEditUtilisateur(
  currentRole: RoleType | null | undefined,
  currentUserId: number | null | undefined,
  target: Utilisateur
): boolean {
  if (!currentRole || target.statut !== "ACTIF") {
    return false;
  }

  if (currentRole === Role.ADMINISTRATEUR) {
    return true;
  }

  if (currentRole === Role.BIBLIOTHECAIRE) {
    return target.role === Role.ADHERENT;
  }

  return !!currentUserId && currentUserId === target.id;
}

export function getEditableRolesForTarget(
  currentRole: RoleType | null | undefined,
  targetRole: RoleType | null | undefined
): RoleType[] {
  if (currentRole === Role.ADMINISTRATEUR) {
    if (targetRole === Role.ADMINISTRATEUR) {
      return [Role.ADMINISTRATEUR];
    }
    return [Role.ADHERENT, Role.BIBLIOTHECAIRE];
  }

  if (currentRole === Role.BIBLIOTHECAIRE) {
    return [Role.ADHERENT];
  }

  return [];
}

export function shouldLockUtilisateurRoleEdition(params: {
  currentRole: RoleType | null | undefined;
  currentUserId: number | null | undefined;
  targetUserId: number | null | undefined;
  targetRole: RoleType | null | undefined;
}): boolean {
  const {
    currentRole,
    currentUserId,
    targetUserId,
    targetRole,
  } = params;

  if (!currentRole) {
    return false;
  }

  if (currentRole === Role.ADMINISTRATEUR) {
    return currentUserId === targetUserId && targetRole === Role.ADMINISTRATEUR;
  }

  if (currentRole === Role.BIBLIOTHECAIRE) {
    return true;
  }

  return false;
}

export function canAdminSelfDowngrade(params: {
  currentRole: RoleType | null | undefined;
  currentUserId: number | null | undefined;
  targetUserId: number | null | undefined;
  nextRole: RoleType | null | undefined;
}): boolean {
  const {
    currentRole,
    currentUserId,
    targetUserId,
    nextRole,
  } = params;

  if (currentRole !== Role.ADMINISTRATEUR) {
    return true;
  }

  if (currentUserId !== targetUserId) {
    return true;
  }

  return nextRole === Role.ADMINISTRATEUR;
}
