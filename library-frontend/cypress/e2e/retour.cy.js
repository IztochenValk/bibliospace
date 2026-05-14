/**
 * E2E — Retour d'un emprunt en cours.
 *
 * Chaque test suit le pattern Arrange / Act / Assert.
 */
describe("Retour", () => {
  beforeEach(() => {
    cy.login();
  });

  it("retourne un livre emprunte", () => {
    // ARRANGE : l'adherent est connecte et consulte la liste de ses
    //           emprunts en cours.
    cy.visit("/mes-emprunts");

    // ACT : clic sur le premier bouton 'Retourner' (premier emprunt
    //       EN_COURS de la liste).
    cy.contains("Retourner").first().click();

    // ASSERT : confirmation visible — le back a bascule l'emprunt en
    //          statut RETOURNE (PATCH /api/emprunts/{id}/retour).
    cy.contains(/retourné|succès/i).should("exist");
  });
});
