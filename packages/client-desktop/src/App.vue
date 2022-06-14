<template>
  <NConfigProvider :theme="darkTheme">


    <AppTitleBar v-if="titlebar" />
    <Snackbar />
    <NotFoundModal v-if="notFoundModal" />
    <div class="embedded">
      <router-view />
    </div>
  </NConfigProvider>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue';
import AppTitleBar from './components/layout/AppTitleBar.vue';
import Snackbar from './components/global/Snackbar/index.vue';
import { SettingsStore } from './stores/SettingsStore';
import { AuthenticationStore } from './stores/AuthenticationStore';
import { AppStore } from './stores/AppStore';
import { ClientStore } from './stores/ClientStore';
import { Client } from './lib/Client';
import NotFoundModal from './components/app/Modal/NotFoundModal.vue';
import { User } from './lib/structures/Users';
import { NConfigProvider, darkTheme, lightTheme } from 'naive-ui';
export default defineComponent({
  name: 'App',
  beforeCreate() {
    this.$.appContext.config.globalProperties.$prod = window.prod;
    if (!window.prod) {
      window.addScript('http://localhost:8098');
    }
  },
  setup() {
    let app = AppStore();
    let client = ClientStore();
    let settings = SettingsStore();
    app.load();
    settings.load();
    let instance = new Client({
      store: client,
      rest: {
        api: (window.prod) ? 'https://api.zenspace.cf' : 'http://localhost:8081',
        headers: {
          'x-usmell': true,
        },
        retries: 3,
        version: 2,
      },
      socket: {
        compression: 'zlib',
        region: 'US',
        account: null,
        properties: {
          os: 'Windows',
          browser: 'Native',
          build: AppStore().version,
        },
      },
    });
    client.setClient(instance);
    let authentication = AuthenticationStore();
    authentication.load().then(() => {
      authentication.accounts.forEach((v, k) => {
        // @ts-ignore
        client.accounts.set(k, new User(client.client, v));
      });
    });

    return {
      titlebar: computed(() => app.titlebar),
      notFoundModal: computed(() => app.notFoundModal),
      darkTheme,
      lightTheme
    };
  },
  async mounted() {
    // invoke('get_battery_percentage').then(percent => {
    //   let batt = Number(5)
    //   if (batt <= 30) {
    //       if (batt <= 15) {
    //          snackbar.create('error', `Device running critically low on power ${batt}%`, true, 1500);
    //       } else {
    //         snackbar.create('warning', `Device running low on power ${batt}%`, true, 1500);
    //       }
    //   }
    // })
  },
  components: { AppTitleBar, Snackbar, NotFoundModal, NConfigProvider, },
});
</script>

<style lang="scss">
@import './assets/sass/base';

#app,
html,
body,
.embedded {
  height: 100vh;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s;
}

.fade-enter,
.fade-leave-to {
  opacity: 0;
}

.slither-enter-active,
.slither-leave-active {
  transition: transform 1s;
}

.slither-enter,
.slither-leave-to {
  transform: translateX(-100%);
}

.slither-enter-to,
.slither-leave {
  transform: translateX(0);
}
</style>
