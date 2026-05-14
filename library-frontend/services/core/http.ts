import { toApiThrownError } from "~/services/core/httpErrors";

type HttpMethod = "GET" | "POST" | "PUT" | "PATCH" | "DELETE";

type HttpOptions = {
  method?: HttpMethod;
  body?: BodyInit | Record<string, unknown> | null;
  auth?: boolean;
  headers?: Record<string, string>;
};

/**
 * Wrapper HTTP unique du frontend.
 *
 * Quatre responsabilites transverses :
 *  - injection automatique du Bearer JWT (si options.auth = true)
 *  - fixation du Content-Type application/json par defaut (sauf pour
 *    les uploads multipart : si le body est un FormData, on laisse le
 *    navigateur fixer lui-meme le boundary)
 *  - transformation des erreurs HTTP en Error JS enrichies (.status, .api)
 *  - interception globale du 401 sur les requetes authentifiees :
 *    purge du state d'auth + redirection vers /login plutot que
 *    laisser remonter l'erreur brute vers la page (qui afficherait
 *    "[GET] /api/xxx: 401" dans une alerte)
 */
export async function http<T>(url: string, options: HttpOptions = {}): Promise<T> {
  const auth = useAuth();

  const headers: Record<string, string> = {
    ...(options.headers || {}),
  };

  const isMultipart = typeof FormData !== "undefined" && options.body instanceof FormData;

  if (options.body !== undefined && options.body !== null && !isMultipart) {
    headers["Content-Type"] = "application/json";
  }

  if (options.auth && auth.state.value.accessToken) {
    headers.Authorization = `Bearer ${auth.state.value.accessToken}`;
  }

  try {
    return await $fetch<T>(url, {
      baseURL: String(useRuntimeConfig().public.apiBase || ""),
      method: options.method || "GET",
      headers,
      body: options.body as BodyInit | Record<string, unknown> | null | undefined,
    });
  } catch (err: unknown) {
    const thrown = toApiThrownError(err);

    // 401 sur requete authentifiee = JWT expire ou invalide. On purge le
    // state local et on redirige proprement vers /login. La page appelante
    // ne verra qu'une exception generique apres redirection — aucune
    // alerte brute du type "[GET] /api/xxx: 401" n'est affichee.
    if (thrown.status === 401 && options.auth && import.meta.client) {
      auth.clearAuth();
      await navigateTo("/login");
    }

    throw thrown;
  }
}
