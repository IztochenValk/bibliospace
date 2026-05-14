import { computed } from "vue";
import { useToast } from "~/composables/ui/useToast";
import type { UtilisateurResponse } from "~/types/utilisateurs";
import type { Role as RoleType } from "~/types/shared";
import {
  getEditableRolesForTarget,
  shouldLockUtilisateurRoleEdition,
  canAdminSelfDowngrade,
} from "~/services/utilisateursPolicies";

export function useUtilisateurEditPolicies(targetId: Readonly<{ value: number }>, utilisateur: Readonly<{ value: UtilisateurResponse | null }>) {
  const auth = useAuth();
  const { push } = useToast();

  const lockRole = computed(() => {
    return shouldLockUtilisateurRoleEdition({
      currentRole: auth.state.value.role,
      currentUserId: auth.state.value.userId,
      targetUserId: targetId.value,
      targetRole: utilisateur.value?.role ?? null,
    });
  });

  const allowedRoles = computed(() => {
    return getEditableRolesForTarget(
      auth.state.value.role,
      utilisateur.value?.role ?? null
    );
  });

  function validateRoleChange(nextRole: RoleType): boolean {
    const allowed = canAdminSelfDowngrade({
      currentRole: auth.state.value.role,
      currentUserId: auth.state.value.userId,
      targetUserId: targetId.value,
      nextRole,
    });

    if (!allowed) {
      push("error", "Un administrateur ne peut pas se rétrograder lui-même");
      return false;
    }

    return true;
  }

  return {
    lockRole,
    allowedRoles,
    validateRoleChange,
  };
}
