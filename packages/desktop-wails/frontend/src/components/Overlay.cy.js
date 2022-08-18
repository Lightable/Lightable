import Overlay from "./Overlay.vue"

describe('<Overlay />', () => {
  it('renders', () => {
    // see: https://test-utils.vuejs.org/guide/
    cy.mount(Overlay)
  })
})