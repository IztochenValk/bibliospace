export function toDate(value: string | null | undefined): Date | null {
  if (!value) return null;
  const d = new Date(value);
  return isNaN(d.getTime()) ? null : d;
}

export function toIsoString(date: Date | null | undefined): string | null {
  if (!date) return null;
  return date.toISOString();
}
