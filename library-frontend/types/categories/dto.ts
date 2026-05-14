export type CreateCategorieRequest = {
  nomCategorie: string;
};

export type UpdateCategorieRequest = {
  nomCategorie: string;
};

export type CategorieResponse = {
  id: number;
  nomCategorie: string,
  utilisee: boolean
};

export type CategorieSummaryResponse = {
  id: number;
  nomCategorie: string;
};
