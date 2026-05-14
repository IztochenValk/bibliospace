import { Role } from "~/types/shared";

export function useBibliothecaireGuard() {
  const auth = useAuth();

  const ready = ref(false);
  const allowed = ref(false);

  async function checkAccess() {
    if (import.meta.server) {
      return;
    }

    ready.value = false;
    allowed.value = false;

    await auth.ensureHydrated();

    if (!auth.isAuthed.value) {
      ready.value = true;
      allowed.value = false;
      await navigateTo("/login");
      return;
    }

    await auth.ensureProfileLoaded();

    if (
      auth.role.value !== Role.BIBLIOTHECAIRE &&
      auth.role.value !== Role.ADMINISTRATEUR
    ) {
      ready.value = true;
      allowed.value = false;
      await navigateTo("/catalogue");
      return;
    }

    allowed.value = true;
    ready.value = true;
  }

  return {
    ready,
    allowed,
    checkAccess,
  };
}
