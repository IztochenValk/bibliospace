import { canAccessAdminArea } from "~/services/auth/authPermissions";

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

  if (!canAccessAdminArea(role.value)) {
    return navigateTo("/");
  }
});
