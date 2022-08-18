import ChatInput from "./ChatInput.vue"

describe('<ChatInput />', () => {
  it('renders', () => {
    // see: https://test-utils.vuejs.org/guide/
    cy.mount(ChatInput)
  })
})