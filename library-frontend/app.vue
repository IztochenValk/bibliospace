<template>
  <div class="app-shell">
    <a href="#main-content" class="skip-link">
      Aller au contenu principal
    </a>

    <PixelBg />

    <div class="app-content">
      <NuxtLayout>
        <main id="main-content" tabindex="-1">
          <NuxtPage />
        </main>
      </NuxtLayout>
    </div>

    <AppToast />
  </div>
</template>

<script setup lang="ts">
import PixelBg from "~/components/app/PixelBg.vue";
import AppToast from "~/components/app/AppToast.vue";

// Métadonnées globales (titre, favicon, thème, viewport, lang)
useHead({
  htmlAttrs: {
    lang: "fr",
  },
  titleTemplate: (title) => (title ? `${title} — BiblioSpace` : "BiblioSpace"),
  meta: [
    { charset: "utf-8" },
    { name: "viewport", content: "width=device-width, initial-scale=1, viewport-fit=cover" },
    { name: "theme-color", content: "#0d1425" },
    { name: "color-scheme", content: "dark" },
    {
      name: "description",
      content:
        "BiblioSpace, la plateforme moderne de gestion de bibliothèque : catalogue, emprunts et administration, le tout dans une interface unifiée.",
    },
  ],
  link: [
    { rel: "icon", type: "image/x-icon", href: "/favicon.ico" },
    { rel: "icon", type: "image/png", sizes: "16x16", href: "/favicon-16.png" },
    { rel: "icon", type: "image/png", sizes: "32x32", href: "/favicon-32.png" },
    { rel: "apple-touch-icon", sizes: "180x180", href: "/apple-touch-icon.png" },
  ],
});

// Open Graph et Twitter Card par défaut
useSeoMeta({
  ogSiteName: "BiblioSpace",
  ogType: "website",
  ogLocale: "fr_FR",
  ogTitle: "BiblioSpace — Plateforme de gestion de bibliothèque",
  ogDescription:
    "BiblioSpace, la plateforme moderne de gestion de bibliothèque : catalogue, emprunts et administration.",
  ogImage: "/og-image.jpg",
  ogImageWidth: 1200,
  ogImageHeight: 630,
  ogImageAlt: "Logo BiblioSpace",
  twitterCard: "summary_large_image",
  twitterTitle: "BiblioSpace",
  twitterDescription:
    "Plateforme moderne de gestion de bibliothèque : catalogue, emprunts et administration.",
  twitterImage: "/og-image.jpg",
});
</script>
