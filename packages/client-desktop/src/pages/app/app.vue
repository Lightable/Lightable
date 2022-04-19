<template>
  <div class="app">
    <Loading v-if="state != 2" :notice="notice" :state="state" />
    <div class="app-data">
      <AddFriendModal v-if="addFriendModalVis"/>
      <NewNameModal v-if="newNameModalVis"/>
      <SideBar v-if="true" />
       <FriendContextMenu v-if="currentPanel == 'Friends'"/>
       <SettingsContextMenu v-if="currentPanel == 'Settings'"/>
       <Profile v-if="currentlyActiveProfile != null" :profile="currentlyActiveProfile"/>
       <OfflineModal v-if="offline"/>
    </div>
  </div>
  <router-view />
</template>

<script lang="ts">
import {computed, defineComponent} from 'vue';
import Loading from '@/components/global/loading.vue';
import {appWindow, LogicalSize} from '@tauri-apps/api/window';
import {AppStore} from '@/stores/AppStore';
import {ClientStore} from '@/stores/ClientStore';
import SideBar from '../../components/app/SideBar.vue';
import UnravelAni from '@/components/global/SideContext/UnravelAni.vue';
import SideContextMenu from '@/components/global/SideContext/SideContextMenu.vue';
import Friend from '@/components/global/Icons/Friend.vue';
import FriendContextMenu from '@/components/global/SideContext/FriendContextMenu.vue';
import AddFriendModal from '@/components/global/AddFriendModal.vue';
import SettingsContextMenu from '@/components/global/SideContext/SettingsContext/SettingsContextMenu.vue';
import NewNameModal from '../../components/global/NewNameModal.vue';
import Profile from '@/components/app/User/Profile.vue';
import OfflineModal from '@/components/global/OfflineModal.vue';
export default defineComponent({
  components: { Loading, SideBar, UnravelAni, SideContextMenu, Friend, FriendContextMenu, AddFriendModal, SettingsContextMenu, NewNameModal, Profile, OfflineModal },
  name: 'app',
  setup() {
    let clientStore = ClientStore();
    let appStore = AppStore();
    return {
      notice: computed(() => clientStore.notice),
      state: computed(() => clientStore.state),
      currentPanel: computed(() => appStore.currentPanelAct),
      addFriendModalVis: computed(() => appStore.addFriendModalVis),
      newNameModalVis: computed(() => appStore.newNameModalVis),
      currentlyActiveProfile: computed(() => appStore.currentlyActiveProfile),
      offline: computed(() => appStore.offline)
    };
  },
  async mounted() {
    let clientStore = ClientStore();
    if (!clientStore.client?.self) {
      this.$router.push('/');
    }
  },
});
</script>

<style scoped lang="scss">
.app-data {
  display: flex;
  flex-direction: row;
}
</style>
