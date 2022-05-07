<template>
  <div class="app">
    <Loading v-if="state != 2" :notice="notice" :state="state" />
    <div class="app-data">
      <AddFriendModal v-if="addFriendModalVis" />
      <NewNameModal v-if="newNameModalVis" />
      <SideBar v-if="state == 2" />
      <FriendContextMenu v-if="currentPanel == 'Friends'" />
      <AdminContextMenu v-if="currentPanel == 'Admin'" />
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
import {computed, defineComponent} from 'vue';
import Loading from '@/components/global/loading.vue';
import {AppStore} from '@/stores/AppStore';
import {ClientStore} from '@/stores/ClientStore';
import SideBar from '@/components/app/SideBar.vue';
import UnravelAni from '@/components/layout/SideTab/_extensions/UnravelAni.vue';
import SideContextMenu from '@/components/layout/SideTab/index.vue';
import Friend from '@/components/Icons/Friend.vue';
import FriendContextMenu from '@/components/layout/SideTab/Friends/index.vue';
import AdminContextMenu from '@/components/layout/SideTab/Admin/index.vue';
import AddFriendModal from '@/components/app/Modal/AddFriendModal.vue';
import SettingsContextMenu from '@/components/layout/SideTab/Settings/index.vue';
import NewNameModal from '@/components/app/Modal/NewNameModal.vue';
import Profile from '@/components/app/User/Profile.vue';
import OfflineModal from '@/components/app/Modal/OfflineModal.vue';
import TooManyConnections from '@/components/app/Modal/TooManyConnections.vue';
import AvatarEditor from '@/components/app/User/Avatar/_extensions/AvatarEditor.vue';
import {AuthenticationStore} from '@/stores/AuthenticationStore';
import {appWindow, LogicalSize} from '@tauri-apps/api/window';
import NewReleaseModal from '../../components/app/Modal/_admin/NewReleaseModal.vue';
import UpdateModal from '@/components/app/Modal/UpdateModal.vue';
export default defineComponent({
  components: {
    Loading,
    SideBar,
    UnravelAni,
    SideContextMenu,
    Friend,
    FriendContextMenu,
    AddFriendModal,
    SettingsContextMenu,
    NewNameModal,
    Profile,
    OfflineModal,
    TooManyConnections,
    AvatarEditor,
    AdminContextMenu,
    NewReleaseModal,
    UpdateModal,
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
      appWindow.center();
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
