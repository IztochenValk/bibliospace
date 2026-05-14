import { computed, ref } from "vue";
import { useToast } from "~/composables/ui/useToast";
import type { ApiThrownError } from "~/services/core/httpErrors";
import { extractLoginErrorMessage } from "~/utils/errors/authErrors";
import { getDefaultRouteForRole } from "~/services/auth/authRouting";

export function useLoginPage() {
  const auth = useAuth();
  const { push } = useToast();

  const email = ref("");
  const password = ref("");
  const showPassword = ref(false);
  const loading = ref(false);
  const formError = ref("");

  const canSubmit = computed(() => {
    return !!email.value.trim() && !!password.value.trim();
  });

  function clearError() {
    formError.value = "";
  }

  async function submit() {
    if (!email.value.trim() || !password.value.trim()) {
      formError.value = "Email et mot de passe requis";
      push("error", formError.value);
      return;
    }

    loading.value = true;
    formError.value = "";

    try {
      await auth.login(email.value.trim(), password.value);
      await auth.ensureProfileLoaded();

      push("success", "Connexion réussie");
      await navigateTo(getDefaultRouteForRole(auth.role.value));
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      formError.value = extractLoginErrorMessage(error);
      push("error", formError.value);
    } finally {
      loading.value = false;
    }
  }

  return {
    email,
    password,
    showPassword,
    loading,
    formError,
    canSubmit,
    clearError,
    submit,
  };
}
