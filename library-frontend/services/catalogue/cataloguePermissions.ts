import { canBorrowBooks } from "~/services/auth/authPermissions";
import type { Role } from "~/types/shared";
import type { CatalogueItemResponse } from "~/types/catalogue";

export function canBorrowCatalogueItem(
  role: Role | null | undefined,
  userId: number | null | undefined,
  item: CatalogueItemResponse | null | undefined
): boolean {
  if (!canBorrowBooks(role)) {
    return false;
  }

  if (!userId || !item) {
    return false;
  }

  if (item.quantiteDisponible <= 0) {
    return false;
  }

  if (item.emprunteParUtilisateur) {
    return false;
  }

  return true;
}
