import tailwindcss from "@tailwindcss/vite";

export default defineNuxtConfig({
  compatibilityDate: "2026-02-04",
  css: ["~/assets/css/main.css"],

  vite: {
    plugins: [tailwindcss()],
  },

  runtimeConfig: {
    public: {
      // ?? au lieu de || : on veut préserver une chaîne vide explicite
      // (cas de la prod où front et back sont sur la même origine via nginx
      // → apiBase doit être "" pour produire des URLs relatives /api/...).
      // Avec ||, "" serait considéré falsy et tomberait sur le fallback dev.
      apiBase: process.env.NUXT_PUBLIC_API_BASE ?? "http://localhost:8080",
    },
  },

  modules: ["@nuxtjs/color-mode"],
});
