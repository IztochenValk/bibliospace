export type CatalogueItem = {
  livreId: number;
  titre: string | null;
  auteur: string | null;
  description: string | null;
  imageUrl: string | null;
  quantiteTotale: number;
  quantiteDisponible: number;
  emprunteParUtilisateur: boolean;
};
