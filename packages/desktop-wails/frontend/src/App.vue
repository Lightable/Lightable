<script lang="ts" setup>
import {computed} from 'vue';
import { NConfigProvider, darkTheme, lightTheme, NElement } from 'naive-ui';
import Titlebar from './components/Titlebar.vue';
import LeftDrawer from './components/LeftDrawer.vue';
import { useAppStore } from './stores/AppStore';
const appStore = useAppStore();
appStore.load();

let theme = computed(() => appStore.theme);
</script>

<template>
  <div class="lightable-green">
    <NConfigProvider :theme="(theme == 'Dark') ? darkTheme : lightTheme">
      <NElement>
        <Titlebar />
        <div class="general-co">
          <div class="lightable-drawer hide">
            <LeftDrawer />
          </div>
          <div class="page" :style="{'background': ($router.currentRoute.value.name == 'login') ? 'transparent' : `${theme == 'Dark' ? 'var(--lightable-dark-bg)' : 'var(--lightable-light-bg)'}`}">
            <div class="content">
              <router-view />
            </div>
          </div>
        </div>
      </NElement>
    </NConfigProvider>
  </div>
</template>

<style lang="scss" scoped>
.lightable-green {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  height: 100vh;

  .general-co {
    height: 100vh;
    width: 100%;
    display: flex;
    flex-direction: row;

    .lightable-drawer {
      display: flex;
      flex-direction: column;
      width: 250px;
      height: 100%;
      background-color: rgba(51, 51, 51, 0.055);
    }

    .page {
      width: 100%;
      // background-color: #121219;
      display: flex;
      flex-direction: column;
      justify-content: flex-end;

      .content {
        width: 100%;
        height: calc(100% - 32px);
      }
    }
  }
}
</style>
