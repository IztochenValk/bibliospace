/**
 * E2E — Creation d'un livre par un bibliothecaire.
 *
 * Couvre deux cas, chacun structure en Arrange / Act / Assert :
 *  - le garde-fou cote front qui refuse le submit si la langue n'est pas
 *    selectionnee (validation locale, sans appel reseau)
 *  - le parcours nominal : remplissage du formulaire, soumission, toast
 *    de succes, redirection vers la page de detail.
 */
describe("Création d'un livre", () => {
  beforeEach(() => {
    // login bibliothecaire (fixture user.json)
    cy.login();
    cy.visit("/livres/nouveau");
  });

  it("affiche un toast d'erreur si la langue n'est pas sélectionnée (garde-fou client)", () => {
    // ARRANGE : remplissage partiel — titre et auteur OK, langue
    //           volontairement laissee vide.
    cy.get("input[name=titre]").type("Livre sans langue");
    cy.get("input[name=auteur]").type("Test Author");
    // On ne touche PAS au select langue.

    // ACT : tentative de soumission.
    cy.get("button[type=submit]").click();

    // ASSERT : le garde-fou client (useLivreCreatePage.submit early return)
    //          a affiche un toast mentionnant 'langue' SANS faire d'appel
    //          reseau — l'URL reste sur /livres/nouveau.
    cy.contains(/langue/i).should("be.visible");
    cy.url().should("include", "/livres/nouveau");
  });

  it("crée un livre et redirige vers sa page de detail (cas nominal)", () => {
    // ARRANGE : remplissage complet du formulaire avec un titre unique
    //           pour eviter le conflit d'unicite titre+auteur en BDD.
    const titreUnique = `Test Cypress ${Date.now()}`;

    cy.get("input[name=titre]").type(titreUnique);
    cy.get("input[name=auteur]").type("Auteur E2E");
    cy.get("select[name=langue]").select("FR");
    cy.get("input[name=quantiteTotale]").clear().type("2");

    // ACT : soumission du formulaire.
    cy.get("button[type=submit]").click();

    // ASSERT : trois conditions doivent etre reunies pour valider la
    //          chaine complete (front -> http() -> back -> JSON -> front).
    cy.contains(/succès|créé/i).should("be.visible");           // toast
    cy.url().should("match", /\/livres\/\d+$/);                   // redirection
    cy.contains(titreUnique).should("be.visible");                // detail rendu
  });
});
