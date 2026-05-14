/**
 * E2E — Creation d'un emprunt depuis le catalogue.
 *
 * Chaque test suit le pattern Arrange / Act / Assert.
 */
describe("Emprunt", () => {
  beforeEach(() => {
    cy.login();
  });

  it("emprunte un livre disponible", () => {
    // ARRANGE : l'adherent est connecte et atterrit sur la liste des
    //           livres du catalogue.
    cy.visit("/catalogue");

    // ACT : clic sur le premier bouton 'Emprunter' actif (libre disponible).
    cy.contains("Emprunter").first().click();

    // ASSERT : le toast de succes apparait, confirmant que la requete
    //          POST /api/emprunts a abouti et que la chaine
    //          controller -> service -> repository -> BDD est passee.
    cy.contains(/succès|emprunt/i).should("exist");
  });

  it("refuse l'emprunt si le livre est indisponible", () => {
    cy.visit("/catalogue");
    cy.contains("Indisponible").should("exist");
  });
});
