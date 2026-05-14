import type { Role } from "~/types/shared";

export type AuthIdentity = {
  userId: number;
  nom: string;
  prenom: string;
  email: string;
  role: Role;
};
