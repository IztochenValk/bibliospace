import { canAccessBibliothecaireArea } from "~/services/auth/authPermissions";

export default defineNuxtRouteMiddleware(async () => {
  if (import.meta.server) {
    return;
  }

  const { ensureHydrated, ensureProfileLoaded, isAuthed, role } = useAuth();

  await ensureHydrated();

  if (!isAuthed.value) {
    return navigateTo("/login");
  }

  await ensureProfileLoaded();

  if (!canAccessBibliothecaireArea(role.value)) {
    return navigateTo("/");
  }
});
