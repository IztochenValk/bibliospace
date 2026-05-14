import { computed, ref } from "vue";
import { useToast } from "~/composables/ui/useToast";
import { getUtilisateurById, updateUtilisateur } from "~/services/utilisateursService";
import type { UtilisateurResponse } from "~/types/utilisateurs";
import { Role } from "~/types/shared";
import type { ApiThrownError } from "~/services/core/httpErrors";
import { extractHttpErrorMessage } from "~/utils/errors/apiErrors";

type UtilisateurFormValue = {
  nom: string;
  prenom: string;
  email: string;
  role: Role;
};

export function useUtilisateurEditPage() {
  const { push } = useToast();

  const utilisateur = ref<UtilisateurResponse | null>(null);
  const loading = ref(false);
  const saving = ref(false);
  const errorMessage = ref("");

  const overrides = { 404: "Utilisateur introuvable" } as const;

  async function fetchUtilisateur(id: number) {
    loading.value = true;
    errorMessage.value = "";

    try {
      utilisateur.value = await getUtilisateurById(id);
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      errorMessage.value = extractHttpErrorMessage(
        error,
        "Erreur lors du chargement de l'utilisateur",
        overrides,
      );
      utilisateur.value = null;
    } finally {
      loading.value = false;
    }
  }

  const initialForm = computed<UtilisateurFormValue>(() => {
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

  async function submit(id: number, value: UtilisateurFormValue) {
    saving.value = true;
    errorMessage.value = "";

    try {
      const updated = await updateUtilisateur(id, {
        nom: value.nom,
        prenom: value.prenom,
        email: value.email,
        role: value.role,
      });

      utilisateur.value = updated;
      push("success", "Utilisateur mis à jour avec succès");
      await navigateTo(`/utilisateurs/${id}`);
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      errorMessage.value = extractHttpErrorMessage(
        error,
        "Impossible de mettre à jour l'utilisateur",
        overrides,
      );
      push("error", errorMessage.value);
    } finally {
      saving.value = false;
    }
  }

  return {
    utilisateur,
    loading,
    saving,
    errorMessage,
    initialForm,
    fetchUtilisateur,
    submit,
  };
}
