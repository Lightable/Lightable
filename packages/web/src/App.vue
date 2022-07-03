<script setup lang="ts">
// This starter template is using Vue 3 <script setup> SFCs
// Check out https://vuejs.org/api/sfc-script-setup.html#script-setup
import { NConfigProvider, NDialogProvider, darkTheme, lightTheme } from 'naive-ui';
import { computed } from 'vue';
import ZHeader from './components/ZHeader.vue';
import { useAppStore } from './stores/AppStore';
import { useClientStore } from './stores/ClientStore';
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
    <NConfigProvider :theme="(isDark) ? darkTheme : lightTheme">
      <NDialogProvider>
        <ZHeader />
        <router-view />
      </NDialogProvider>
    </NConfigProvider>
  </div>
</template>

<style>
#app {
  font-family: strawfordregular;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  background-color: var(--bg-light);
}

.route {
  width: 100%;
  height: 100vh;
}
</style>
