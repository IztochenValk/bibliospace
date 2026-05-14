import { ref, computed } from "vue";
import { useToast } from "~/composables/ui/useToast";
import {
  listUtilisateurs,
  deactivateUtilisateur,
  reactivateUtilisateur,
  anonymizeUtilisateur,
} from "~/services/utilisateursService";
import {
  canDeleteUtilisateur,
  canEditUtilisateur,
  canReactivateUtilisateur,
  canAnonymizeUtilisateur,
} from "~/services/utilisateursPolicies";
import type { UtilisateurResponse } from "~/types/utilisateurs";
import type { ApiThrownError } from "~/services/core/httpErrors";
import { extractHttpErrorMessage } from "~/utils/errors/apiErrors";

type PendingActionType = "deactivate" | "reactivate" | "anonymize";

type PendingAction = {
  type: PendingActionType;
  user: UtilisateurResponse;
};

export function useUtilisateursPage() {
  const { push } = useToast();
  const auth = useAuth();
  const loading = ref(false);
  const actionLoadingId = ref<number | null>(null);
  const items = ref<UtilisateurResponse[]>([]);
  const pendingAction = ref<PendingAction | null>(null);

  const currentRole = computed(() => auth.state.value.role);
  const currentUserId = computed(() => auth.state.value.userId);

  const currentPage = ref(1);
  const pageSize = ref(10);
  const pageSizeOptions = [10, 15, 20];

  const totalItems = computed(() => items.value.length);

  const totalPages = computed(() => {
    return Math.max(1, Math.ceil(totalItems.value / pageSize.value));
  });

  const paginatedItems = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value;
    const end = start + pageSize.value;
    return items.value.slice(start, end);
  });

  function normalizePage() {
    if (currentPage.value > totalPages.value) {
      currentPage.value = totalPages.value;
    }

    if (currentPage.value < 1) {
      currentPage.value = 1;
    }
  }

  function goToPage(page: number) {
    currentPage.value = Math.min(Math.max(1, page), totalPages.value);
  }

  function setPageSize(size: number) {
    if (!pageSizeOptions.includes(size)) {
      return;
    }

    pageSize.value = size;
    currentPage.value = 1;
    normalizePage();
  }

  async function fetchUtilisateurs() {
    loading.value = true;

    try {
      const all = await listUtilisateurs();
      // On exclut le user courant : sa propre fiche est accessible via
      // /me (Mon compte), pas via la liste de gestion des utilisateurs.
      // Cela évite la double porte d'entrée vers le même profil pour
      // les comptes staff (BIBLIOTHECAIRE et ADMINISTRATEUR).
      const myId = currentUserId.value;
      items.value = myId != null ? all.filter((u) => u.id !== myId) : all;
      normalizePage();
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      push("error", extractHttpErrorMessage(error, "Impossible de charger les utilisateurs"));
    } finally {
      loading.value = false;
    }
  }

  function canDelete(user: UtilisateurResponse): boolean {
    return canDeleteUtilisateur(
      currentRole.value,
      currentUserId.value,
      user
    );
  }

  function canReactivate(user: UtilisateurResponse): boolean {
    return canReactivateUtilisateur(
      currentRole.value,
      currentUserId.value,
      user
    );
  }

  function canAnonymize(user: UtilisateurResponse): boolean {
    return canAnonymizeUtilisateur(
      currentRole.value,
      currentUserId.value,
      user
    );
  }

  function canEdit(user: UtilisateurResponse): boolean {
    return canEditUtilisateur(
      currentRole.value,
      currentUserId.value,
      user
    );
  }

  async function remove(user: UtilisateurResponse) {
    actionLoadingId.value = user.id;

    try {
      await deactivateUtilisateur(user.id);
      push("success", "Utilisateur désactivé avec succès");
      await fetchUtilisateurs();
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      push("error", extractHttpErrorMessage(error, "Impossible de désactiver cet utilisateur"));
    } finally {
      actionLoadingId.value = null;
    }
  }

  async function reactivate(user: UtilisateurResponse) {
    actionLoadingId.value = user.id;

    try {
      await reactivateUtilisateur(user.id);
      push("success", "Utilisateur réactivé avec succès");
      await fetchUtilisateurs();
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      push("error", extractHttpErrorMessage(error, "Impossible de réactiver cet utilisateur"));
    } finally {
      actionLoadingId.value = null;
    }
  }

  async function anonymize(user: UtilisateurResponse) {
    actionLoadingId.value = user.id;

    try {
      await anonymizeUtilisateur(user.id);
      push("success", "Utilisateur anonymisé avec succès");
      await fetchUtilisateurs();
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      push("error", extractHttpErrorMessage(error, "Impossible d'anonymiser cet utilisateur"));
    } finally {
      actionLoadingId.value = null;
    }
  }

  // ==============================
  // Flux de confirmation
  // ==============================
  // Les actions destructives (desactiver, anonymiser) exigent une
  // confirmation explicite. Le composant ConfirmModal affiche un
  // message contextuel reconstruit a partir de pendingAction.

  const confirmTitle = computed(() => {
    const action = pendingAction.value;
    if (!action) return "";
    if (action.type === "deactivate") return "Désactiver cet utilisateur ?";
    if (action.type === "reactivate") return "Réactiver cet utilisateur ?";
    return "Anonymiser cet utilisateur ?";
  });

  const confirmMessage = computed(() => {
    const action = pendingAction.value;
    if (!action) return "";
    const name = `${action.user.prenom} ${action.user.nom}`;
    if (action.type === "deactivate") {
      return `${name} ne pourra plus se connecter.\nLes emprunts en cours seront automatiquement retournés.`;
    }
    if (action.type === "reactivate") {
      return `${name} pourra de nouveau se connecter.`;
    }
    return `Les données personnelles de ${name} seront définitivement anonymisées.\nCette action est irréversible.`;
  });

  const confirmVariant = computed<"danger" | "warning" | "primary">(() => {
    const action = pendingAction.value;
    if (!action) return "primary";
    if (action.type === "anonymize") return "danger";
    if (action.type === "deactivate") return "warning";
    return "primary";
  });

  const confirmLabel = computed(() => {
    const action = pendingAction.value;
    if (!action) return "Confirmer";
    if (action.type === "deactivate") return "Désactiver";
    if (action.type === "reactivate") return "Réactiver";
    return "Anonymiser";
  });

  const isConfirming = computed(() => actionLoadingId.value !== null);

  function askRemove(user: UtilisateurResponse) {
    pendingAction.value = { type: "deactivate", user };
  }

  function askReactivate(user: UtilisateurResponse) {
    pendingAction.value = { type: "reactivate", user };
  }

  function askAnonymize(user: UtilisateurResponse) {
    pendingAction.value = { type: "anonymize", user };
  }

  function cancelPendingAction() {
    pendingAction.value = null;
  }

  async function confirmPendingAction() {
    const action = pendingAction.value;
    if (!action) return;

    if (action.type === "deactivate") {
      await remove(action.user);
    } else if (action.type === "reactivate") {
      await reactivate(action.user);
    } else if (action.type === "anonymize") {
      await anonymize(action.user);
    }

    pendingAction.value = null;
  }

  return {
    loading,
    actionLoadingId,
    items,
    paginatedItems,
    currentPage,
    pageSize,
    pageSizeOptions,
    totalItems,
    totalPages,
    fetchUtilisateurs,
    canDelete,
    canReactivate,
    canAnonymize,
    canEdit,
    remove,
    reactivate,
    anonymize,
    goToPage,
    setPageSize,

    // Flux de confirmation
    pendingAction,
    confirmTitle,
    confirmMessage,
    confirmVariant,
    confirmLabel,
    isConfirming,
    askRemove,
    askReactivate,
    askAnonymize,
    confirmPendingAction,
    cancelPendingAction,
  };
}
