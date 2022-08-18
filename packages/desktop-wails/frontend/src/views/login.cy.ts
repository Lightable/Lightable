import login from "./login.vue"
import {createTestingPinia} from '@pinia/testing';
describe('<login />', () => {
  it('renders', () => {
    // see: https://test-utils.vuejs.org/guide/
    cy.mount(login, {
      global: {
        plugins: [createTestingPinia({createSpy: null})]
      }
    })
  })
})