import { computed } from "vue";
import type { Role } from "~/types/shared";
import { useAuthApi } from "~/services/auth/authApi";
import { getMyProfile } from "~/services/meService";
import { buildAuthStateFromToken, buildAuthIdentityFromMe } from "~/services/auth/authSessionService";

/**
 * Composable central d'authentification.
 *
 * Flux aligne sur le backend simplifie (formateur-style) :
 *
 *  login(email, password)
 *    -> POST /api/auth/login              -> { token }
 *    -> decodage client du JWT            -> uid, email, role, exp
 *    -> state peuple : accessToken + identite minimale
 *    -> ensureProfileLoaded() enrichit nom/prenom via /api/utilisateurs/me
 *
 *  logout()
 *    -> clearAuth() local uniquement.
 *       Pas d'endpoint serveur (le JWT est stateless, le backend formateur
 *       n'expose pas de /logout). Le token reste valide cote serveur
 *       jusqu'a son expiration naturelle.
 */

type AuthUserState = {
  accessToken: string | null;
  expiresAt: number | null;
  userId: number | null;
  nom: string | null;
  prenom: string | null;
  email: string | null;
  role: Role | null;
};

type AuthLoginPayload = {
  accessToken: string;
  expiresAt: number | null;
  userId: number | null;
  nom?: string | null;
  prenom?: string | null;
  email?: string | null;
  role: Role | null;
};

const AUTH_STORAGE_KEY = "library_auth";

function getDefaultAuthState(): AuthUserState {
  return {
    accessToken: null,
    expiresAt: null,
    userId: null,
    nom: null,
    prenom: null,
    email: null,
    role: null,
  };
}

function isClient(): boolean {
  return typeof window !== "undefined";
}

function safeParseAuthStorage(value: string | null): Partial<AuthUserState> | null {
  if (!value) {
    return null;
  }

  try {
    return JSON.parse(value) as Partial<AuthUserState>;
  } catch {
    return null;
  }
}

export function useAuth() {
  const authApi = useAuthApi();

  const state = useState<AuthUserState>("auth.state", getDefaultAuthState);
  const hydrated = useState<boolean>("auth.hydrated", () => false);
  const profileLoaded = useState<boolean>("auth.profileLoaded", () => false);

  function persistState() {
    if (!isClient()) {
      return;
    }

    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(state.value));
  }

  function loadStateFromStorage() {
    if (!isClient()) {
      return;
    }

    const parsed = safeParseAuthStorage(localStorage.getItem(AUTH_STORAGE_KEY));

    if (!parsed) {
      return;
    }

    state.value = {
      ...getDefaultAuthState(),
      ...parsed,
    };
  }

  function clearAuth() {
    state.value = getDefaultAuthState();
    hydrated.value = true;
    profileLoaded.value = false;

    if (isClient()) {
      localStorage.removeItem(AUTH_STORAGE_KEY);
    }
  }

  async function ensureHydrated(): Promise<void> {
    if (hydrated.value) {
      return;
    }

    loadStateFromStorage();

    // Si le JWT stocke est expire, on nettoie immediatement.
    if (state.value.accessToken && state.value.expiresAt && Date.now() >= state.value.expiresAt) {
      clearAuth();
    }

    hydrated.value = true;
  }

  function setAuth(payload: AuthLoginPayload) {
    state.value = {
      accessToken: payload.accessToken,
      expiresAt: payload.expiresAt,
      userId: payload.userId,
      nom: payload.nom ?? null,
      prenom: payload.prenom ?? null,
      email: payload.email ?? null,
      role: payload.role,
    };

    hydrated.value = true;
    profileLoaded.value = !!(payload.nom && payload.prenom);
    persistState();
  }

  function patchProfile(payload: {
    nom?: string | null;
    prenom?: string | null;
    email?: string | null;
    role?: Role | null;
    userId?: number | null;
  }) {
    state.value = {
      ...state.value,
      nom: payload.nom ?? state.value.nom,
      prenom: payload.prenom ?? state.value.prenom,
      email: payload.email ?? state.value.email,
      role: payload.role ?? state.value.role,
      userId: payload.userId ?? state.value.userId,
    };

    profileLoaded.value = true;
    persistState();
  }

  /**
   * Charge le profil complet (nom, prenom, role) depuis le backend.
   * Appele apres le login et sur les routes protegees quand le state
   * est hydrate depuis le localStorage mais pas encore verifie.
   */
  async function ensureProfileLoaded(): Promise<void> {
    await ensureHydrated();

    if (profileLoaded.value) {
      return;
    }

    if (!state.value.accessToken) {
      profileLoaded.value = false;
      return;
    }

    // Si on a deja toutes les infos en cache, pas besoin de rappeler l'API.
    if (state.value.userId && state.value.role && state.value.email && state.value.nom) {
      profileLoaded.value = true;
      return;
    }

    try {
      const me = await getMyProfile();
      const identity = buildAuthIdentityFromMe(me);

      state.value = {
        ...state.value,
        userId: identity.userId ?? state.value.userId,
        nom: identity.nom ?? state.value.nom,
        prenom: identity.prenom ?? state.value.prenom,
        email: identity.email ?? state.value.email,
        role: identity.role ?? state.value.role,
      };

      profileLoaded.value = true;
      persistState();
    } catch {
      // Token rejete par le serveur (ex. 401) -> session invalide.
      clearAuth();
    }
  }

  async function login(email: string, password: string) {
    const response = await authApi.loginRequest(email, password);

    const fromToken = buildAuthStateFromToken(response.token);

    setAuth({
      accessToken: fromToken.accessToken ?? response.token,
      expiresAt: fromToken.expiresAt ?? null,
      userId: fromToken.userId ?? null,
      email: fromToken.email ?? email,
      role: fromToken.role ?? null,
      nom: null,
      prenom: null,
    });
  }

  /**
   * Logout purement cote client : on detruit le state et le localStorage.
   * Le JWT emis reste valide cote serveur jusqu'a son expiration naturelle,
   * mais sans le token, le client ne peut plus l'utiliser.
   */
  async function logout(): Promise<void> {
    clearAuth();
  }

  const isAuthenticated = computed(() => !!state.value.accessToken);
  const isAuthed = computed(() => !!state.value.accessToken);
  const email = computed(() => state.value.email);
  const role = computed(() => state.value.role);
  const fullName = computed(() => {
    return [state.value.prenom, state.value.nom].filter(Boolean).join(" ").trim();
  });

  return {
    state,
    hydrated,
    profileLoaded,

    isAuthenticated,
    isAuthed,
    email,
    role,
    fullName,

    setAuth,
    patchProfile,
    clearAuth,
    ensureHydrated,
    ensureProfileLoaded,
    login,
    logout,
  };
}
