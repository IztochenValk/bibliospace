import { computed, ref } from "vue";
import { useToast } from "~/composables/ui/useToast";
import { getMyProfile, updateMyProfile, updateMyPassword } from "~/services/meService";
import { Role } from "~/types/shared";
import type { ApiThrownError } from "~/services/core/httpErrors";
import { extractHttpErrorMessage } from "~/utils/errors/apiErrors";

type MeFormValue = {
  nom: string;
  prenom: string;
  email: string;
  role: Role;
};

export function useMePage() {
  const auth = useAuth();
  const { push } = useToast();

  const loading = ref(false);
  const saving = ref(false);
  const passwordSaving = ref(false);
  const errorMessage = ref("");
  const passwordErrorMessage = ref("");

  const utilisateur = ref<{
    id: number;
    nom: string;
    prenom: string;
    email: string;
    role: Role;
    statut: "ACTIF" | "INACTIF" | "ANONYMISE";
  } | null>(null);

  const overrides = { 404: "Utilisateur introuvable" } as const;

  const userId = computed(() => auth.state.value.userId);

  const initialForm = computed<MeFormValue>(() => {
    if (!utilisateur.value) {
      return {
        nom: "",
        prenom: "",
        email: "",
        role: Role.ADHERENT,
      };
    }

    return {
      nom: utilisateur.value.nom,
      prenom: utilisateur.value.prenom,
      email: utilisateur.value.email,
      role: utilisateur.value.role,
    };
  });

  async function fetchMeProfile() {
    errorMessage.value = "";
    loading.value = true;

    try {
      await auth.ensureHydrated();
      await auth.ensureProfileLoaded();

      if (!userId.value) {
        throw Object.assign(new Error("Utilisateur non authentifié"), { status: 401 });
      }

      utilisateur.value = await getMyProfile();
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      utilisateur.value = null;
      errorMessage.value = extractHttpErrorMessage(error, "Impossible de charger votre compte", overrides);
      push("error", errorMessage.value);
    } finally {
      loading.value = false;
    }
  }

  async function submitProfile(value: { nom: string; prenom: string; email: string }) {
    errorMessage.value = "";
    saving.value = true;

    try {
      await auth.ensureHydrated();
      await auth.ensureProfileLoaded();

      if (!userId.value || !utilisateur.value) {
        throw Object.assign(new Error("Utilisateur non authentifié"), { status: 401 });
      }

      const updated = await updateMyProfile({
        nom: value.nom.trim(),
        prenom: value.prenom.trim(),
        email: value.email.trim(),
      });

      utilisateur.value = updated;

      auth.patchProfile({
        userId: updated.id,
        nom: updated.nom,
        prenom: updated.prenom,
        email: updated.email,
        role: updated.role,
      });

      push("success", "Compte mis à jour avec succès");
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      errorMessage.value = extractHttpErrorMessage(error, "Impossible de mettre à jour votre compte", overrides);
      push("error", errorMessage.value);
    } finally {
      saving.value = false;
    }
  }

  async function submitPassword(value: { ancienMotDePasse: string; nouveauMotDePasse: string }) {
    passwordErrorMessage.value = "";
    passwordSaving.value = true;

    try {
      await auth.ensureHydrated();
      await auth.ensureProfileLoaded();

      if (!userId.value) {
        throw Object.assign(new Error("Utilisateur non authentifié"), { status: 401 });
      }

      await updateMyPassword({
        ancienMotDePasse: value.ancienMotDePasse,
        nouveauMotDePasse: value.nouveauMotDePasse,
      });

      push("success", "Mot de passe mis à jour avec succès");
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      passwordErrorMessage.value = extractHttpErrorMessage(error, "Impossible de modifier le mot de passe", overrides);
      push("error", passwordErrorMessage.value);
    } finally {
      passwordSaving.value = false;
    }
  }

  return {
    utilisateur,
    loading,
    saving,
    passwordSaving,
    errorMessage,
    passwordErrorMessage,
    initialForm,
    fetchMeProfile,
    submitProfile,
    submitPassword,
  };
}
