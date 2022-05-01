<template>
  <LoginBG>
    <Overlay>
      <div class="accounts">
        <h2>Pick An Account Below</h2>
        <div class="inner">
          <Account
            v-for="account in accounts.values()"
            v-bind:key="account._id"
            :account="account"
            class="actual-acc"
          />
      
        </div>
            <button class="db onboard" @click="$router.push('login')">Use Another Account</button>
      </div>
    </Overlay>
  </LoginBG>
</template>

<script lang="ts">
import {defineComponent, computed} from 'vue';
import {appWindow, LogicalSize} from '@tauri-apps/api/window';
import {AppStore} from '@/stores/AppStore';
import LoginBG from './login/LoginBG.vue';
import Overlay from '@/components/app/Modal/_base/Overlay.vue';
import Account from '@/components/app/Self/Accounts/Account.vue';
import {ClientStore} from '@/stores/ClientStore';
export default defineComponent({
  name: 'AccountPicker',
  components: {LoginBG, Overlay, Account},
  setup(props) {
    let app = AppStore();
    app.allowTitlebar(false);
    appWindow.setSize(new LogicalSize(320, 550));
    appWindow.setResizable(false);
    appWindow.center();
    appWindow.setFocus();
    appWindow.setTitle('Chatty • Login');
    return {
      accounts: computed(() => ClientStore().accounts),
    };
  },
});
</script>

<style lang="scss" scoped>
.accounts {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  max-height: 100vh;
  min-height: 100vh;
  color: var(--blue);
  h2 {
    padding: 8px;
    border-radius: 8px;
  }
  .onboard {
      margin-right: auto;
      margin-left: auto;
      margin-top: auto;
    }
  .inner {
    width: 98%;
    height: 100%;
    overflow-y: scroll;
    &::-webkit-scrollbar {
      display: none;
    }
    
    .actual-acc {
      margin-top: 5px;
    }
  }
}
</style>