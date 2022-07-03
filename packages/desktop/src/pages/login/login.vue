<template>
  <div class="login">
    <LoginBG>
      <div class="actions" data-tauri-drag-region>
        <svg
          xmlns="http://www.w3.org/2000/svg"
          data-tauri-drag-region
          height="24px"
          viewBox="0 0 24 24"
          width="24px"
          fill="#FFFFFF"
        >
          <path d="M0 0h24v24H0V0z" fill="none" />
          <path
            d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"
          />
        </svg>
      </div>
      <div class="form">
        <div class="login-icon" :failed="failed">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="icon"
            height="24px"
            viewBox="0 0 24 24"
            width="64px"
            fill="var(--bg)"
          >
            <path d="M0 0h24v24H0V0z" fill="none" />
            <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z" />
          </svg>
        </div>
        <div class="body">
          <h2>Welcome Back!</h2>
          <span>Log in to your existing account</span>
          <div class="inputs">
            <input
              type="email"
              class="email"
              placeholder="Enter your email"
              :failed="failed"
              v-model="state.email"
            />
            <input
              type="password"
              class="password"
              placeholder="Enter your password"
              :failed="failed"
              v-model="state.password"
            />
            <button class="login" @click="login" :sending="sending" :failed="failed">
              <span>{{ stateLogin }}</span>
            </button>
          </div>
        </div>
      </div>
    </LoginBG>
  </div>
</template>

<script lang="ts">
import {AppStore} from '@/stores/AppStore';
import {defineComponent} from 'vue';
import LoginBG from './LoginBG.vue';
import {appWindow, LogicalSize} from '@tauri-apps/api/window';
import {SnackStore} from '@/stores/SnackStore';
import {ClientStore} from '@/stores/ClientStore';
import hash from '@/lib/utils/hash';
import {AuthenticationStore} from '@/stores/AuthenticationStore';
export default defineComponent({
  name: 'login',
  components: {LoginBG},
  setup() {
    let app = AppStore();
    app.allowTitlebar(false);
    appWindow.setSize(new LogicalSize(320, 550));
    appWindow.setResizable(false);
    appWindow.center();
    appWindow.setFocus();
    appWindow.setTitle('ZenSpace • Login');
    ClientStore().$resetData();
  },
  methods: {
    async login(e: any) {
      let snackstore = SnackStore();
      let client = ClientStore().client;
      let auth = AuthenticationStore();
      let appStore = AppStore();
      this.sending = true;
      this.stateLogin = 'Logging In...';
      try {
        let user = await client?.req('POST', '/user/@me/login', undefined, {
          email: this.state.email,
          password: this.state.password,
        });
        console.log('Client ', client, 'User', user);
        if (!user) {
          this.sending = false;
          this.failed = true;
          snackstore.create('error', 'Login Failed', true, 2000);
          setTimeout(() => {
            this.failed = false;
            this.stateLogin = 'LOG IN';
          }, 1000);
        }
        
        // @ts-ignore
        if (user.token) {
          // @ts-ignore
          snackstore.create('success', `Welcome Back  ${user.name}`, true, 2000);
          user!!.authentication = user.token.token;
          auth.addAccount(user!!);
          await client?.loginWT(user.token.token);
          await auth.save();

          appWindow.setSize(new LogicalSize(1220, 700));
          appWindow.setResizable(true);
          appStore.allowTitlebar(true);
          appStore.setTitleDetails('Home');
          this.$router.push('/app/channels/@me');
          return;
        }
      } catch (e: any) {
        this.stateLogin = 'Failed :c';
        this.sending = false;
        this.failed = true;
        console.log(e);
        snackstore.create('error', 'Login Failed', true, 2000);
        setTimeout(() => {
          this.failed = false;
          this.stateLogin = 'LOG IN';
        }, 1000);
      }
    },
  },
  data() {
    return {
      sending: false as boolean,
      stateLogin: 'LOG IN' as string,
      failed: false as boolean,
      state: {
        email: '',
        password: '',
      },
    };
  },
});
</script>

<style lang="scss" scoped>
@import '@/assets/sass/ui/input.scss';
.login {
  padding: 8px;
  transition: all 250ms ease;
  .actions {
    position: absolute;
    top: 16px;
    right: 16px;
    height: 32px;
    width: 32px;
    color: var(--white);
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    svg {
      position: absolute;
      z-index: 1;
    }
  }
}
.form {
  display: flex;
  align-items: center;
  margin-top: 20%;
  height: 100vh;
  backdrop-filter: blur(2px);
  flex-direction: column;
  align-content: center;
  .body {
    margin-top: 15px;
    color: var(--white);
    align-items: center;
    user-select: none;
    text-align: center;
    .inputs {
      margin-top: 15%;
      display: flex;
      align-items: center;
      flex-direction: column;
      .login {
        width: 90%;
        background: var(--blue);
        margin-top: 35px;
        outline: none;
        border: none;
        display: flex;
        justify-content: center;
        align-items: center;
        height: 35px;
        font-size: 1em;
        border-radius: 8px;
        color: var(--white);
        font-weight: bold;
        box-shadow: 0 2px 5px rgba(0, 0, 0, 0.25);

        &:hover {
          filter: brightness(90%);
        }
        &[sending='true'] {
          filter: brightness(80%);
          animation: brightAnimation 0.7s infinite alternate ease;
          cursor: not-allowed;
        }
        &[failed='true'] {
          filter: 80%;
          background: var(--error);
          cursor: not-allowed;
        }
        cursor: pointer;
        span {
          margin-top: 2px;
        }
      }
      input {
        @include input($border-radius: 6px);
        margin-top: 15px;
        width: 85%;
      }
    }
  }
  .login-icon {
    padding: 19px;
    border-radius: 50%;
    background: var(--blue);
    display: flex;
    justify-content: center;
    align-items: center;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.25);
    &[failed='true'] {
      background: var(--error);
    }
    .icon {
      height: 50px;
      width: 50px;
    }
  }
  input {
    height: 30px;
    color: var(--white) !important;
  }
}
h1 {
  color: var(--blue);
  margin-top: 120px;
  font-size: 3em;
  img {
    margin-right: 5px;
    justify-items: middle;
    margin-top: auto;
    margin-bottom: auto;
  }
}
@keyframes brightAnimation {
  from {
    filter: brightness(60%);
  }
  to {
    filter: brightness(110%);
  }
}
</style>
