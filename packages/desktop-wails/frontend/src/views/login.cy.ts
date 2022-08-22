import login from "./login.vue"
import {createTestingPinia} from '@pinia/testing';

describe('<login />', () => {
  it('renders', () => {
    // see: https://test-utils.vuejs.org/guide/
    // @ts-ignore
    cy.mount(login, {
      global: {
        // @ts-ignore
        plugins: [createTestingPinia({createSpy: null})]
      }
    })
  })
})