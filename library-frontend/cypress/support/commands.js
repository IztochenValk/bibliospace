Cypress.Commands.add("login", () => {
  cy.fixture("user").then((user) => {
    cy.visit("/login");

    cy.get("input[name=email]").type(user.email);
    cy.get("input[name=motDePasse]").type(user.motDePasse);

    cy.get("button[type=submit]").click();

    cy.url().should("not.include", "/login");
  });
});
