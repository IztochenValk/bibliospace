export function useImageUrl() {
  const config = useRuntimeConfig();

  function resolveImageUrl(value?: string | null): string {
    const raw = String(value || "").trim();

    if (!raw) {
      return "";
    }

    if (
      raw.startsWith("http://") ||
      raw.startsWith("https://") ||
      raw.startsWith("data:") ||
      raw.startsWith("blob:")
    ) {
      return raw;
    }

    const base = String(config.public.apiBase || "").replace(/\/$/, "");
    const path = raw.startsWith("/") ? raw : `/${raw}`;

    return `${base}${path}`;
  }

  return {
    resolveImageUrl,
  };
}
