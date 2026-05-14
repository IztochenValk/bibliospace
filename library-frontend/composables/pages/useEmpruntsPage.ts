import { computed, ref, watch } from "vue";
import { useToast } from "~/composables/ui/useToast";
import {
  listEmprunts,
  rechercherEmprunts,
  retournerEmprunt,
} from "~/services/empruntsService";
import type { EmpruntResponse } from "~/types/emprunts";
import { Role, type StatutEmprunt as StatutEmpruntType } from "~/types/shared";
import type { ApiThrownError } from "~/services/core/httpErrors";
import { extractHttpErrorMessage } from "~/utils/errors/apiErrors";

type StatutFilter = "TOUS" | StatutEmpruntType;

/**
 * Composable unifié pour la page /emprunts.
 *
 * Gère la liste des emprunts ET la recherche, avec un comportement
 * différencié selon le rôle de l'utilisateur courant :
 *
 * - ADHERENT  → appelle GET /api/emprunts (scopé serveur sur ses emprunts).
 *               Filtres affichés côté front : titre + statut.
 *
 * - STAFF     → appelle GET /api/emprunts/recherche?nom=X&titre=Y.
 *               La JPQL gère les filtres vides (LIKE '%%' matche tout).
 *               Filtres affichés côté front : nom-ou-prénom + titre + statut.
 *
 * Le filtre statut est appliqué côté client dans tous les cas (filtrage local
 * sur la liste retournée), pour une expérience instantanée sans aller-retour
 * serveur à chaque changement de dropdown.
 *
 * Les filtres texte (nom, titre) sont debouncés à 300ms pour éviter de
 * saturer le serveur pendant la frappe.
 */
export function useEmpruntsPage() {
  const { push } = useToast();
  const auth = useAuth();

  // -------- État réactif --------
  const items = ref<EmpruntResponse[]>([]);
  const loading = ref(false);
  const actionLoadingId = ref<number | null>(null);
  const errorMessage = ref("");

  // Filtres
  const nomFilter = ref("");
  const titreFilter = ref("");
  const statutFilter = ref<StatutFilter>("TOUS");

  const isStaff = computed(
    () => auth.role.value === Role.BIBLIOTHECAIRE || auth.role.value === Role.ADMINISTRATEUR
  );

  // -------- Liste filtrée (statut côté client) --------
  const filteredItems = computed(() => {
    if (statutFilter.value === "TOUS") {
      return items.value;
    }
    return items.value.filter((e) => e.statut === statutFilter.value);
  });

  // -------- Fetch --------
  async function fetchEmprunts() {
    loading.value = true;
    errorMessage.value = "";

    try {
      if (isStaff.value) {
        // Staff : on tape la JPQL recherche, qui gère filtres vides.
        items.value = await rechercherEmprunts(
          nomFilter.value.trim(),
          titreFilter.value.trim()
        );
      } else {
        // Adhérent : liste scopée serveur de ses propres emprunts.
        items.value = await listEmprunts();
      }
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      items.value = [];
      errorMessage.value = extractHttpErrorMessage(
        error,
        "Impossible de charger les emprunts"
      );
      push("error", errorMessage.value);
    } finally {
      loading.value = false;
    }
  }

  // -------- Debounce sur les inputs texte --------
  let debounceTimer: ReturnType<typeof setTimeout> | null = null;
  function debouncedFetch() {
    if (debounceTimer) clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      fetchEmprunts();
    }, 300);
  }

  // Refetch quand un filtre serveur change (nom ou titre).
  // Pour adhérent, le titre est filtré CÔTÉ CLIENT donc on ne refetch pas.
  watch([nomFilter, titreFilter], () => {
    if (isStaff.value) {
      debouncedFetch();
    }
  });

  // Pour les adhérents, le titre est aussi filtré côté client
  // (ils ne touchent pas le filtre nom).
  const visibleItems = computed(() => {
    if (isStaff.value) {
      return filteredItems.value;
    }
    // Adhérent : filtre titre côté client (en plus de statut)
    const titre = titreFilter.value.trim().toLowerCase();
    if (!titre) {
      return filteredItems.value;
    }
    return filteredItems.value.filter((e) =>
      (e.titreLivre || "").toLowerCase().includes(titre)
    );
  });

  // -------- Actions --------
  async function retour(item: EmpruntResponse) {
    if (item.statut !== "EN_COURS") {
      return;
    }

    actionLoadingId.value = item.id;

    try {
      await retournerEmprunt(item.id, {});
      push("success", "Livre retourné avec succès");
      await fetchEmprunts();
    } catch (err: unknown) {
      const error = err as ApiThrownError;
      push("error", extractHttpErrorMessage(error, "Impossible de retourner ce livre"));
    } finally {
      actionLoadingId.value = null;
    }
  }

  function resetFilters() {
    nomFilter.value = "";
    titreFilter.value = "";
    statutFilter.value = "TOUS";
    fetchEmprunts();
  }

  return {
    // état
    items: visibleItems,
    loading,
    actionLoadingId,
    errorMessage,
    isStaff,

    // filtres
    nomFilter,
    titreFilter,
    statutFilter,

    // actions
    fetchEmprunts,
    retour,
    resetFilters,
  };
}
