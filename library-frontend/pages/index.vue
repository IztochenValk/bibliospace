<template>
  <section class="-mx-4 -mt-8">
    <!-- ===== HERO ===== -->
    <!-- Hero transparent : on laisse le PixelBg global de l'app respirer
         derrière. Aucun fond, aucun halo, aucune étoile décorative. -->
    <div class="relative">
      <div class="relative mx-auto flex w-full max-w-6xl flex-col items-center gap-10 px-4 py-20 md:py-28 lg:flex-row lg:py-32">
        <!-- Texte hero -->
        <div class="flex-1 text-center lg:text-left">
          <div class="mb-4 inline-flex items-center gap-2 rounded-full border border-primary/40 bg-primary/10 px-3 py-1 text-xs font-medium text-primary">
            <span class="h-2 w-2 animate-pulse rounded-full bg-primary"></span>
            Plateforme de gestion de bibliothèque
          </div>

          <h1 class="text-4xl font-extrabold leading-tight tracking-tight md:text-5xl lg:text-6xl">
            Bienvenue sur
            <span class="bg-gradient-to-r from-primary via-warning to-accent bg-clip-text text-transparent">
              BiblioSpace
            </span>
          </h1>

          <p class="mx-auto mt-5 max-w-xl text-base-content/75 md:text-lg lg:mx-0">
            Votre bibliothèque moderne, connectée et pensée pour simplifier le quotidien
            des adhérents comme du personnel. Explorez le catalogue, empruntez, gérez
            le fonds documentaire — le tout dans une interface unifiée.
          </p>

          <div
            v-if="auth.hydrated.value"
            class="mt-8 flex flex-wrap justify-center gap-3 lg:justify-start"
          >
            <template v-if="auth.isAuthed.value">
              <NuxtLink :to="entryRoute" class="btn btn-primary btn-lg shadow-lg shadow-primary/30">
                Accéder à l'application
                <svg xmlns="http://www.w3.org/2000/svg" class="ml-1 h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M13 7l5 5m0 0l-5 5m5-5H6" />
                </svg>
              </NuxtLink>
              <NuxtLink to="/me" class="btn btn-ghost btn-lg">
                Mon compte
              </NuxtLink>
            </template>

            <template v-else>
              <NuxtLink to="/login" class="btn btn-primary btn-lg shadow-lg shadow-primary/30">
                Se connecter
                <svg xmlns="http://www.w3.org/2000/svg" class="ml-1 h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M13 7l5 5m0 0l-5 5m5-5H6" />
                </svg>
              </NuxtLink>
              <a href="#features" class="btn btn-ghost btn-lg">
                Découvrir
              </a>
            </template>
          </div>
        </div>

        <!-- Logo en vedette -->
        <div class="flex flex-1 items-center justify-center">
          <div class="relative">
            <!-- Halo léger derrière le logo : une seule couche, opacité faible,
                 blur modéré → effet d'aura discret sans déborder sur le texte. -->
            <div
              class="absolute inset-0 rounded-full bg-primary/15 blur-2xl"
              aria-hidden="true"
            ></div>
            <picture>
              <source srcset="/bibliospace-logo.webp" type="image/webp" />
              <img
                src="/bibliospace-logo.png"
                alt="Logo BiblioSpace"
                width="288"
                height="288"
                class="relative h-56 w-56 md:h-72 md:w-72 object-contain float-slow"
              />
            </picture>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== FEATURES ===== -->
    <div id="features" class="mx-auto w-full max-w-6xl px-4 py-16">
      <div class="mb-10 text-center">
        <h2 class="text-3xl font-bold md:text-4xl">Une interface pensée pour chacun</h2>
        <p class="mt-3 text-base-content/70">
          Des fonctionnalités adaptées à votre rôle, avec des permissions fines.
        </p>
      </div>

      <div class="grid gap-6 md:grid-cols-2">
        <!-- Carte Adhérent -->
        <article class="group card border border-base-300/60 bg-base-200/40 shadow-xl transition hover:-translate-y-1 hover:border-info/60 hover:shadow-info/20">
          <div class="card-body">
            <div class="mb-2 flex items-center gap-3">
              <div class="rounded-xl bg-info/15 p-3 text-info">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                </svg>
              </div>
              <h3 class="card-title text-info">Côté adhérent</h3>
            </div>
            <p class="text-base-content/80">
              Parcourez le catalogue, empruntez en un clic quand un exemplaire est disponible,
              et retrouvez l'historique de vos emprunts et la gestion de votre compte au même endroit.
            </p>
          </div>
        </article>

        <!-- Carte Personnel -->
        <article class="group card border border-base-300/60 bg-base-200/40 shadow-xl transition hover:-translate-y-1 hover:border-warning/60 hover:shadow-warning/20">
          <div class="card-body">
            <div class="mb-2 flex items-center gap-3">
              <div class="rounded-xl bg-warning/15 p-3 text-warning">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                  <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
              </div>
              <h3 class="card-title text-warning">Côté personnel</h3>
            </div>
            <p class="text-base-content/80">
              Gérez le catalogue et son stock, enregistrez les retours
              et, pour les administrateurs, pilotez les comptes utilisateurs
              depuis des interfaces dédiées.
            </p>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { getDefaultRouteForRole } from "~/services/auth/authRouting";

const auth = useAuth();

const entryRoute = computed(() => {
  return getDefaultRouteForRole(auth.role.value);
});

// SEO spécifique accueil
useSeoMeta({
  title: "Accueil",
  description:
    "Bienvenue sur BiblioSpace, la plateforme moderne de gestion de bibliothèque. Catalogue, emprunts et administration dans une interface unifiée.",
  ogTitle: "BiblioSpace — Votre bibliothèque moderne",
  ogDescription:
    "Explorez le catalogue, empruntez, gérez le fonds documentaire. Une plateforme pensée pour les adhérents et le personnel.",
});
</script>

<style scoped>
/* Animation de flottement légère sur le logo (purement décorative,
   désactivée en cas de prefers-reduced-motion). */
.float-slow {
  animation: float 7s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50%      { transform: translateY(-12px); }
}

@media (prefers-reduced-motion: reduce) {
  .float-slow {
    animation: none !important;
  }
}
</style>
