export function formatUser(
  nom?: string | null,
  prenom?: string | null
): string {
  return [prenom, nom].filter(Boolean).join(" ").trim() || "-";
}
