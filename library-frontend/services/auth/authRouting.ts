import type { Role } from "~/types/shared";
import { Role as Roles } from "~/types/shared";

export function getDefaultRouteForRole(role: Role | null | undefined): string {
  if (role === Roles.ADMINISTRATEUR) {
    return "/utilisateurs";
  }

  if (role === Roles.BIBLIOTHECAIRE) {
    return "/livres";
  }

  return "/catalogue";
}

export function getGuestRedirectRouteForRole(role: Role | null | undefined): string {
  return getDefaultRouteForRole(role);
}

export function getEmpruntsRouteForRole(role: Role | null | undefined): string {
  if (role === Roles.ADMINISTRATEUR) {
    return "/";
  }

  return "/emprunts";
}
