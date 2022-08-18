describe('Login', () => {
  it('passes', () => {
    cy.visit('http://localhost:34115/#/login')
    cy.get('#login-email').type('TestAccount@example.com')
    cy.get('#login-password').type('testpass')
    cy.get('#login-btn').click()
  })
})