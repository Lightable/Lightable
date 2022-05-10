<template>
  <div class="app">
    <Loading v-if="state != 2" :notice="notice" :state="state" />
    <div class="app-data">
      <AddFriendModal v-if="addFriendModalVis" />
      <NewNameModal v-if="newNameModalVis" />
      <SideBar v-if="state == 2" />
      <FriendContextMenu v-if="currentPanel == 'Friends'" />
      <AdminContextMenu v-if="currentPanel == 'Admin'" />
      <ExperimentContextMenu v-if="currentPanel == 'Experiments'"/>
      <SettingsContextMenu v-if="currentPanel == 'Settings'" />
      <Profile v-if="currentlyActiveProfile != null" :profile="currentlyActiveProfile" />
      <OfflineModal v-if="offline" />
      <TooManyConnections v-if="(loggedInDevice != null) | (loggedInDevice != undefined)" />
      <KeepAlive>
        <NewReleaseModal v-if="createReleaseModalVis" />
      </KeepAlive>
      <UpdateModal v-if="downloadingUpdateModalVis" />
      <AvatarEditor v-if="false" />
      <div class="channels">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {computed, defineAsyncComponent, defineComponent} from 'vue';
import Loading from '@/components/global/loading.vue';
import {AppStore} from '@/stores/AppStore';
import {ClientStore} from '@/stores/ClientStore';
import {AuthenticationStore} from '@/stores/AuthenticationStore';
import {appWindow, LogicalSize} from '@tauri-apps/api/window';
export default defineComponent({
  components: {
    Loading,
    SideBar: defineAsyncComponent(() => import('@/components/app/SideBar.vue')),
    UnravelAni: defineAsyncComponent(() => import('@/components/layout/SideTab/_extensions/UnravelAni.vue')),
    SideContextMenu: defineAsyncComponent(() => import('@/components/layout/SideTab/index.vue')),
    Friend: defineAsyncComponent(() => import('@/components/Icons/Friend.vue')),
    FriendContextMenu: defineAsyncComponent(() => import('@/components/layout/SideTab/Friends/index.vue')),
    AddFriendModal: defineAsyncComponent(() => import('@/components/app/Modal/AddFriendModal.vue')),
    SettingsContextMenu: defineAsyncComponent(() => import('@/components/layout/SideTab/Settings/index.vue')),
    NewNameModal: defineAsyncComponent(() => import('@/components/app/Modal/NewNameModal.vue')),
    Profile: defineAsyncComponent(() => import('@/components/app/User/Profile.vue')),
    OfflineModal: defineAsyncComponent(() => import('@/components/app/Modal/OfflineModal.vue')),
    TooManyConnections: defineAsyncComponent(() => import('@/components/app/Modal/TooManyConnections.vue')),
    AvatarEditor: defineAsyncComponent(() => import('@/components/app/User/Avatar/_extensions/AvatarEditor.vue')),
    AdminContextMenu: defineAsyncComponent(() => import('@/components/layout/SideTab/Admin/index.vue')),
    ExperimentContextMenu: defineAsyncComponent(() => import('@/components/layout/SideTab/Experiments/index.vue')),
    NewReleaseModal: defineAsyncComponent(() => import('@/components/app/Modal/_admin/NewReleaseModal.vue')),
    UpdateModal: defineAsyncComponent(() => import('@/components/app/Modal/UpdateModal.vue')),
  },
  name: 'app',
  setup() {
    let clientStore = ClientStore();
    let appStore = AppStore();
    let authStore = AuthenticationStore();
    if (!clientStore.client?.self) {
      if (authStore.default == null) return this.$router.push('/login');
      console.log(authStore.default);
      clientStore.client?.loginWT(authStore.default.authentication);
      appWindow.setSize(new LogicalSize(1220, 700));
      appWindow.setResizable(true);
      appStore.allowTitlebar(true);
      appStore.setTitleDetails('Home');
    }
    return {
      notice: computed(() => clientStore.notice),
      state: computed(() => clientStore.state),
      currentPanel: computed(() => appStore.currentPanelAct),
      addFriendModalVis: computed(() => appStore.addFriendModalVis),
      newNameModalVis: computed(() => appStore.newNameModalVis),
      createReleaseModalVis: computed(() => appStore.newReleaseModalVis),
      downloadingUpdateModalVis: computed(() => appStore.downloadingUpdateModalVis),
      currentlyActiveProfile: computed(() => appStore.currentlyActiveProfile),
      offline: computed(() => appStore.offline),
      loggedInDevice: computed(() => clientStore.connectedDevice),
    };
  },
});
</script>

<style scoped lang="scss">
.app-data {
  display: flex;
  flex-direction: row;
}
.channels {
  width: 100%;
  height: 100vh;
}
</style>
