<template>
  <div class="auth-shell mx-auto flex min-h-[calc(100vh-220px)] w-full items-center justify-center py-10">
    <div class="auth-card w-full max-w-md rounded-2xl border border-base-300 bg-base-100/85 p-6 backdrop-blur">
      <div class="auth-panel space-y-6">
        <div class="space-y-2 text-center">
          <h1 class="text-3xl font-bold tracking-tight text-base-content">
            Connexion
          </h1>
          <p class="text-sm text-base-content/70">
            Connecte-toi à ton espace bibliothèque.
          </p>
        </div>

        <form class="space-y-4" @submit.prevent="submit">
          <label class="form-control w-full" for="login-email">
            <div class="label">
              <span class="label-text">Email</span>
            </div>
            <input
              id="login-email"
              v-model="email"
              type="email"
              placeholder="nom@exemple.com"
              class="input input-bordered input-md w-full auth-input"
              autocomplete="email"
              :disabled="loading"
              :aria-invalid="formError ? 'true' : 'false'"
              aria-describedby="login-form-error"
              @input="clearError"
            />
          </label>

          <label class="form-control w-full" for="login-password">
            <div class="label">
              <span class="label-text">Mot de passe</span>
            </div>

            <div class="relative">
              <input
                id="login-password"
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="Votre mot de passe"
                class="input input-bordered input-md w-full auth-input pr-20"
                autocomplete="current-password"
                :disabled="loading"
                :aria-invalid="formError ? 'true' : 'false'"
                aria-describedby="login-form-error"
                @input="clearError"
              />

              <button
                type="button"
                class="btn btn-ghost btn-xs auth-eye-btn"
                :aria-label="showPassword ? 'Masquer le mot de passe' : 'Afficher le mot de passe'"
                :disabled="loading"
                @click="showPassword = !showPassword"
              >
                {{ showPassword ? "Masquer" : "Afficher" }}
              </button>
            </div>
          </label>

          <div
            v-if="formError"
            id="login-form-error"
            class="rounded-xl border border-error/30 bg-error/10 px-4 py-3 text-sm text-error"
            role="alert"
            aria-live="assertive"
          >
            {{ formError }}
          </div>

          <div class="flex justify-end items-center pt-2 gap-3">
            <button
              class="btn btn-primary auth-primary-btn"
              type="submit"
              :disabled="loading || !canSubmit"
            >
              {{ loading ? "Connexion..." : "Connexion" }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useLoginPage } from "~/composables/pages/useLoginPage";

definePageMeta({
  layout: "default",
  middleware: ["guest"],
});

const {
  email,
  password,
  showPassword,
  loading,
  formError,
  canSubmit,
  clearError,
  submit,
} = useLoginPage();
</script>

<style scoped>
.auth-input {
  background: rgba(13, 20, 37, 0.72);
  border-color: #334155;
  color: #e2e8f0;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.015);
}

.auth-input::placeholder {
  color: #94a3b8;
}

.auth-input:focus,
.auth-input:focus-visible {
  outline: none;
  border-color: rgba(0, 229, 255, 0.35);
  box-shadow:
    0 0 0 1px rgba(0, 229, 255, 0.18),
    0 0 18px rgba(0, 229, 255, 0.08);
}

.auth-eye-btn {
  position: absolute;
  top: 50%;
  right: 0.45rem;
  transform: translateY(-50%);
  min-height: auto;
  height: 1.9rem;
  border: 1px solid transparent;
  color: #94a3b8;
}

.auth-eye-btn:hover {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(51, 65, 85, 0.8);
  color: #e2e8f0;
}

.auth-secondary-btn {
  border: 1px solid #334155;
  background: rgba(13, 20, 37, 0.24);
  color: #e2e8f0;
}

.auth-secondary-btn:hover {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(148, 163, 184, 0.45);
}

.auth-primary-btn {
  box-shadow:
    0 10px 24px rgba(255, 107, 0, 0.18),
    0 0 0 1px rgba(255, 107, 0, 0.08);
}

.auth-primary-btn:hover {
  box-shadow:
    0 12px 28px rgba(255, 107, 0, 0.24),
    0 0 18px rgba(255, 107, 0, 0.18);
}
</style>
