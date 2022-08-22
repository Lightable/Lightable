<script lang="ts" setup>
import { computed } from 'vue';
import { NConfigProvider, NMessageProvider, NDialogProvider, darkTheme, lightTheme, NElement } from 'naive-ui';
import Titlebar from './components/Titlebar.vue';
import LeftDrawer from './components/LeftDrawer/LeftDrawer.vue';
import DrawerComponent from './components/LeftDrawer/DrawerComponent.vue';
import { useAppStore } from './stores/AppStore';
import ConfettiCanvasProvider from './components/confetti/ConfettiCanvasProvider.vue';
import SettingsConfigProvider from './components/settings/SettingsConfigProvider.vue';
const appStore = useAppStore();

darkTheme.Button!!.common!!.errorColor = "#ED4245";
darkTheme.Button!!.common!!.infoColor = "#62CDFE";

const theme = computed(() => appStore.theme);
const leftDrawer = computed(() => appStore.leftDrawer)
appStore.load();
appStore.startRealtime();
</script>

<template>
  <div class="lightable-red">
    <ConfettiCanvasProvider>
      <NConfigProvider :theme="(theme == 'Dark') ? darkTheme : lightTheme">
        <NDialogProvider>
          <NElement>
            <Titlebar />
            <NMessageProvider>
              <SettingsConfigProvider>
                <div class="general-co">
                  <div class="lightable-drawer" v-if="leftDrawer.show">
                    <LeftDrawer>
                      <template #top>
                        <DrawerComponent :pair="item" v-for="(item, index) in leftDrawer.components" v-bind:key="index" />
                      </template>
                      <template #groups>
                        <div class="group" v-for="(group, index) in leftDrawer.groups" v-bind:key="index">
                          <div class="header ns">
                            <span>{{ group.name }}</span>
                          </div>
                          <div class="group-contents" :id="`group-${group.name}`">
                            <div class="group-item" v-for="(gitem, gid) in group.items" v-bind:key="gid" :id="`group-item-${gitem.text}`">
                              <DrawerComponent :pair="gitem" :style="{ width: '88%' }" />
                            </div>
                          </div>
                        </div>
                      </template>
                    </LeftDrawer>
                  </div>
                  <div class="page" :style="{ 'background': ($router.currentRoute.value.name == 'login' || $router.currentRoute.value.name == 'home' || $router.currentRoute.value.name == 'invitesignup' || $router.currentRoute.value.name == 'signup') ? 'transparent' : `${theme == 'Dark' ? 'var(--lightable-dark-bg)' : 'var(--lightable-light-bg)'}` }">
                    <div class="content">
                      <router-view />
                    </div>
                  </div>
                </div>
              </SettingsConfigProvider>
            </NMessageProvider>
          </NElement>
        </NDialogProvider>
      </NConfigProvider>
    </ConfettiCanvasProvider>
  </div>
</template>

<style lang="scss" scoped>
.lightable-red {
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
      min-width: 250px;
      max-width: 250px;
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

.group {
  width: 100%;

  .header {
    margin-left: 10px;
  }

  .group-contents {
    display: flex;
    flex-direction: column;
    width: 100%;
    justify-content: center;
    align-items: flex-start;
    margin-left: 8px;

    .group-item {
      width: 100%;
    }
  }
}
</style>
