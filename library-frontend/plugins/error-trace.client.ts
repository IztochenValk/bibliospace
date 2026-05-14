export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.config.errorHandler = (error, instance, info) => {
    console.error("=== VUE CLIENT ERROR ===");
    console.error("info:", info);
    console.error("component:", instance ? instance.$options?.name || "anonymous-component" : null);
    console.error("error:", error);

    if (error instanceof Error) {
      console.error("message:", error.message);
      console.error("stack:", error.stack);
    }
  };

  window.addEventListener("error", (event) => {
    console.error("=== WINDOW ERROR ===", event.error || event.message);
  });

  window.addEventListener("unhandledrejection", (event) => {
    console.error("=== UNHANDLED PROMISE REJECTION ===", event.reason);
  });
});
