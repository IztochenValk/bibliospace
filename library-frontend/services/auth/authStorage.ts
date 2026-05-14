import type { AuthState } from "~/types";

const AUTH_STORAGE_KEY = "library-auth";

type StoredAuthState = Partial<AuthState>;

export function loadAuthStateFromStorage(): StoredAuthState {
  if (import.meta.server) {
    return {};
  }

  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY);

    if (!raw) {
      return {};
    }

    return JSON.parse(raw) as StoredAuthState;
  } catch {
    return {};
  }
}

export function persistAuthState(state: AuthState): void {
  if (import.meta.server) {
    return;
  }

  try {
    const payload: StoredAuthState = {
      userId: state.userId,
      nom: state.nom,
      prenom: state.prenom,
      email: state.email,
      accessToken: state.accessToken,
      expiresAt: state.expiresAt,
      role: state.role,
    };

    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(payload));
  } catch {
  }
}

export function clearPersistedAuthState(): void {
  if (import.meta.server) {
    return;
  }

  try {
    localStorage.removeItem(AUTH_STORAGE_KEY);
  } catch {
  }
}
