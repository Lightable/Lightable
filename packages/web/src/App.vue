<script setup lang="ts">
// This starter template is using Vue 3 <script setup> SFCs
// Check out https://vuejs.org/api/sfc-script-setup.html#script-setup
import { NConfigProvider, NDialogProvider, darkTheme, lightTheme, NMessageProvider } from 'naive-ui';
import { computed } from 'vue';
import ZHeader from './components/ZHeader.vue';
import { useAppStore } from './stores/AppStore';
import { useClientStore } from './stores/ClientStore';
import ZConfettiCanvasProvider from './components/confetti/ZConfettiCanvasProvider.vue';
let appStore = useAppStore();
let clientStore = useClientStore();
document.getElementById('app')!!.style.backgroundColor = (appStore.isDark) ? 'var(--bg-dark)' : 'var(--bg-light)';
let isDark = computed(() => appStore.isDark);
if (appStore.account) {
  // @ts-ignore
  clientStore.lite.token = appStore.account.token.token;
  clientStore.lite.user = appStore.account;
  clientStore.lite.$getSelf();
}
</script>

<template>
  <div class="route">
    <ZConfettiCanvasProvider>
      <NConfigProvider :theme="(isDark) ? darkTheme : lightTheme">
        <NDialogProvider>
          <ZHeader />
          <NMessageProvider placement="bottom">
            <router-view v-slot="{ Component }">
              <Transition name="nested" :duration="100">
                <component :is="Component" />
              </Transition>
            </router-view>
          </NMessageProvider>
        </NDialogProvider>
      </NConfigProvider>
    </ZConfettiCanvasProvider>
  </div>
</template>

<style>
#app {
  font-family: strawfordregular;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  min-height: 100%;
}

.route {
  width: 100%;
  height: 100vh;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 150ms ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.nested-enter-active,
.nested-leave-active {
  transition: all 0.3s ease-in-out;
}

/* delay leave of parent element */
.nested-leave-active {
  transition-delay: 0.25s;
}

.nested-enter-from,
.nested-leave-to {
  transform: translateY(30px);
  opacity: 0;
}

/* we can also transition nested elements using nested selectors */
.nested-enter-active .inner,
.nested-leave-active .inner {
  transition: all 0.3s ease-in-out;
}

/* delay enter of nested element */
.nested-enter-active .inner {
  transition-delay: 0.25s;
}

.nested-enter-from .inner,
.nested-leave-to .inner {
  transform: translateX(30px);
  /*
  	Hack around a Chrome 96 bug in handling nested opacity transitions.
    This is not needed in other browsers or Chrome 99+ where the bug
    has been fixed.
  */
  opacity: 0.001;
}

/* .bounce-enter-active {
  animation: bounce-in 5s;
}

.bounce-leave-active {
  animation: bounce-out 5s;
}

.bring-enter-active {
  animation: bring-in 150ms ease-in-out ;
}

@keyframes bounce-in {}

@keyframes bounce-out {
  0% {
    transform: scale(1);
    opacity: 1;
  }

  50% {
    transform: scale(1.25);
    opacity: 0.5;
  }

  100% {
    transform: scale(0);
    opacity: 0;
  }
}

@keyframes bring-in {
  0% {
    opacity: 0;
    transform: translateY(50px) translateX(30px);
  }

  100% {
    opacity: 1;
    transform: translateY(0) translateX(0); 
  }
} */
</style>
