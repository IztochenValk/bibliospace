<template>
  <header class="sticky top-0 z-40 border-b border-base-300/60 bg-base-100/80 backdrop-blur-xl">
    <div class="mx-auto flex w-full max-w-7xl items-center justify-between gap-3 px-4 py-2">
      <!-- Logo + nom -->
      <div class="flex items-center gap-2">
        <!-- Bouton hamburger (mobile / tablette) -->
        <button
          type="button"
          class="btn btn-ghost btn-square md:hidden"
          aria-label="Ouvrir le menu de navigation"
          aria-controls="mobile-nav"
          :aria-expanded="mobileOpen ? 'true' : 'false'"
          @click="toggleMobile"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-6 w-6"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            stroke-width="2"
          >
            <path v-if="!mobileOpen" stroke-linecap="round" stroke-linejoin="round" d="M4 6h16M4 12h16M4 18h16" />
            <path v-else stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>

        <NuxtLink
          class="group flex items-center gap-2 rounded-xl px-2 py-1 transition hover:bg-base-200/60"
          to="/"
          aria-label="BiblioSpace - Accueil"
        >
          <picture>
            <source srcset="/bibliospace-logo-512.webp" type="image/webp" />
            <img
              src="/bibliospace-logo-512.png"
              alt=""
              aria-hidden="true"
              width="36"
              height="36"
              class="h-9 w-9 shrink-0 rounded-lg object-contain drop-shadow-[0_0_12px_rgba(255,107,0,0.35)] transition group-hover:drop-shadow-[0_0_16px_rgba(255,107,0,0.6)]"
            />
          </picture>
          <span class="hidden sm:inline text-lg font-bold tracking-tight">
            <span class="text-base-content">Biblio</span><span class="text-primary">Space</span>
          </span>
        </NuxtLink>

        <!-- Nav desktop -->
        <nav
          aria-label="Navigation principale"
          class="hidden md:flex items-center gap-1 ml-2"
        >
          <NuxtLink class="btn btn-ghost btn-sm" to="/">Accueil</NuxtLink>

          <NuxtLink
            v-if="isAuthed && canSeeCatalogue(role)"
            class="btn btn-ghost btn-sm"
            to="/catalogue"
          >
            Catalogue
          </NuxtLink>

          <EmpruntsDropdown :isAuthed="isAuthed" :role="role" />

          <NuxtLink
            v-if="isAuthed && canManageEmprunts(role)"
            class="btn btn-ghost btn-sm"
            to="/emprunts"
          >
            Emprunts
          </NuxtLink>

          <NuxtLink
            v-if="isAuthed && canManageLivres(role)"
            class="btn btn-ghost btn-sm"
            to="/livres"
          >
            Livres
          </NuxtLink>

          <NuxtLink
            v-if="isAuthed && canManageLivres(role)"
            class="btn btn-ghost btn-sm"
            to="/categories"
          >
            Catégories
          </NuxtLink>

          <NuxtLink
            v-if="isAuthed && canManageUtilisateurs(role)"
            class="btn btn-ghost btn-sm"
            to="/utilisateurs"
          >
            Utilisateurs
          </NuxtLink>
        </nav>
      </div>

      <!-- Actions droite (desktop) -->
      <div class="flex items-center gap-3">
        <template v-if="isAuthed">
          <!-- Badge rôle (visible md+) -->
          <span
            v-if="role"
            class="badge badge-outline hidden md:inline-flex"
            :class="roleBadgeClass"
          >
            {{ role }}
          </span>

          <!-- Profile dropdown : icône ronde + carte d'identité au clic -->
          <div v-if="canAccessMe(role)" class="dropdown dropdown-end hidden md:block">
            <div
              tabindex="0"
              role="button"
              class="btn btn-circle btn-ghost btn-sm"
              aria-label="Mon profil"
            >
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"
                   stroke="currentColor" stroke-width="2" stroke-linecap="round"
                   stroke-linejoin="round" class="h-5 w-5">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
            </div>
            <div
              tabindex="0"
              class="dropdown-content z-[20] mt-2 w-64 rounded-box border border-base-300 bg-base-100 p-4 shadow-lg"
            >
              <div class="space-y-2">
                <div class="text-base font-semibold text-base-content">
                  {{ fullName || "—" }}
                </div>
                <div class="text-xs text-base-content/60 break-all">
                  {{ email }}
                </div>
                <div v-if="role" class="pt-1">
                  <span class="badge badge-outline badge-sm" :class="roleBadgeClass">
                    {{ role }}
                  </span>
                </div>
              </div>
              <div class="divider my-2"></div>
              <NuxtLink to="/me" class="btn btn-primary btn-sm w-full">
                Voir mon compte
              </NuxtLink>
            </div>
          </div>

          <!-- Bouton rond : Déconnexion (icône logout, fond rouge) -->
          <button
            class="btn btn-circle btn-sm hidden md:inline-flex bg-error/10 hover:bg-error/20 border-error/30 text-error"
            type="button"
            aria-label="Se déconnecter"
            title="Se déconnecter"
            @click="onLogout"
          >
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round"
                 stroke-linejoin="round" class="h-5 w-5">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
              <polyline points="16 17 21 12 16 7" />
              <line x1="21" y1="12" x2="9" y2="12" />
            </svg>
          </button>
        </template>

        <template v-else>
          <NuxtLink class="btn btn-primary btn-sm" to="/login">Connexion</NuxtLink>
        </template>
      </div>
    </div>

    <!-- Menu mobile (drawer déroulant) -->
    <transition name="slide-fade">
      <nav
        v-if="mobileOpen"
        id="mobile-nav"
        aria-label="Navigation mobile"
        class="md:hidden border-t border-base-300/60 bg-base-100/95 backdrop-blur-xl"
      >
        <ul class="menu menu-lg w-full px-2 py-2">
          <li><NuxtLink to="/" @click="closeMobile">Accueil</NuxtLink></li>

          <li v-if="isAuthed && canSeeCatalogue(role)">
            <NuxtLink to="/catalogue" @click="closeMobile">Catalogue</NuxtLink>
          </li>

          <li v-if="isAuthed && role === 'ADHERENT'">
            <NuxtLink to="/emprunts" @click="closeMobile">Mes emprunts</NuxtLink>
          </li>

          <li v-if="isAuthed && canManageEmprunts(role)">
            <NuxtLink to="/emprunts" @click="closeMobile">Emprunts</NuxtLink>
          </li>
          <li v-if="isAuthed && canManageLivres(role)">
            <NuxtLink to="/livres" @click="closeMobile">Livres</NuxtLink>
          </li>
          <li v-if="isAuthed && canManageLivres(role)">
            <NuxtLink to="/categories" @click="closeMobile">Catégories</NuxtLink>
          </li>
          <li v-if="isAuthed && canManageUtilisateurs(role)">
            <NuxtLink to="/utilisateurs" @click="closeMobile">Utilisateurs</NuxtLink>
          </li>

          <div class="divider my-1"></div>

          <template v-if="isAuthed">
            <li class="px-4 py-1 text-xs text-base-content/60">
              {{ fullName || email }}
              <span
                v-if="role"
                class="badge badge-outline badge-sm ml-1"
                :class="roleBadgeClass"
              >
                {{ role }}
              </span>
            </li>
            <li v-if="canAccessMe(role)">
              <NuxtLink to="/me" @click="closeMobile">Mon compte</NuxtLink>
            </li>
            <li>
              <button type="button" @click="onLogoutMobile">Déconnexion</button>
            </li>
          </template>

          <template v-else>
            <li>
              <NuxtLink to="/login" class="font-semibold text-primary" @click="closeMobile">
                Connexion
              </NuxtLink>
            </li>
          </template>
        </ul>
      </nav>
    </transition>
  </header>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useRoute } from "vue-router";
import EmpruntsDropdown from "~/components/app/EmpruntsDropdown.vue";
import { Role } from "~/types/shared";
import {
  canAccessMe,
  canManageEmprunts,
  canManageLivres,
  canManageUtilisateurs,
  canSeeCatalogue,
} from "~/services/auth/authPermissions";

const {
  isAuthed,
  email,
  fullName,
  role,
  logout,
  ensureProfileLoaded,
} = useAuth();

const mobileOpen = ref(false);
const route = useRoute();

function toggleMobile() {
  mobileOpen.value = !mobileOpen.value;
}

function closeMobile() {
  mobileOpen.value = false;
}

// Ferme le menu mobile à chaque changement de page
watch(() => route.fullPath, () => {
  mobileOpen.value = false;
});

const roleBadgeClass = computed(() => {
  if (role.value === Role.ADMINISTRATEUR) {
    return "border-error text-error";
  }

  if (role.value === Role.BIBLIOTHECAIRE) {
    return "border-warning text-warning";
  }

  if (role.value === Role.ADHERENT) {
    return "border-info text-info";
  }

  return "border-base-content/30 text-base-content/70";
});

onMounted(async () => {
  await ensureProfileLoaded();
});

async function onLogout() {
  await logout();
  await navigateTo("/");
}

async function onLogoutMobile() {
  closeMobile();
  await onLogout();
}
</script>

<style scoped>
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.22s ease;
}
.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
