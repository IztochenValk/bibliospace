import { getGuestRedirectRouteForRole } from "~/services/auth/authRouting";

export default defineNuxtRouteMiddleware(async () => {
  if (import.meta.server) {
    return;
  }

  const { ensureHydrated, ensureProfileLoaded, isAuthed, role } = useAuth();

  await ensureHydrated();

  if (!isAuthed.value) {
    return;
  }

  await ensureProfileLoaded();

  return navigateTo(getGuestRedirectRouteForRole(role.value));
});
