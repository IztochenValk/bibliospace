export default defineNuxtRouteMiddleware(async () => {
  if (import.meta.server) {
    return;
  }

  const { ensureHydrated, isAuthed, clearAuth } = useAuth();

  await ensureHydrated();

  if (isAuthed.value) {
    return;
  }

  clearAuth();
  return navigateTo("/login");
});