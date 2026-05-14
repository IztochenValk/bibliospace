/**
 * E2E — Authentification.
 *
 * Chaque test suit le pattern Arrange / Act / Assert pour rester
 * lisible et deterministe.
 */
describe("Auth", () => {

  it("login fonctionne", () => {
    // ARRANGE : aucune preparation specifique, la commande custom
    //           cy.login() encapsule deja le contexte (fixture user.json,
    //           saisie email + mot de passe).

    // ACT : l'utilisateur s'authentifie.
    cy.login();

    // ASSERT : implicite dans cy.login() qui verifie l'absence de
    //          /login dans l'URL finale (cf. cypress/support/commands.js).
  });

  it("refuse l'accès sans login", () => {
    // ARRANGE : aucun login prealable, on simule un visiteur anonyme.

    // ACT : tentative d'acces direct a une route protegee.
    cy.visit("/dashboard");

    // ASSERT : le middleware Nuxt 'auth' a redirige vers /login.
    cy.url().should("include", "/login");
  });
});
