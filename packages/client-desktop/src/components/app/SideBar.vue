<template>
  <div class="sidebar">
    <SelfItem />
    <SidebarItem
      @hover="hover.friend = true"
      @mouseleave="hover.friend = false"
      @click="currentPanel == 'Friends' ? setCurrentPanel('None') : setCurrentPanel('Friends')"
    >
      <Friend
        :color="hover.friend ? 'var(--ac)' : 'var(--white)'"
        class="icon-tran"
        height="28px"
        width="28px"
        content="Friends"
        v-tippy
      />
    </SidebarItem>
    <SidebarItem
      @click="currentPanel == 'Groups' ? setCurrentPanel('None') : setCurrentPanel('Groups')"
      type="disabled"
    >
      <Group
        color="var(--gray)"
        class="icon-tran"
        height="28px"
        width="28px"
        content="Currently Disabled"
        v-tippy
      />
    </SidebarItem>
    <div class="bottom">
      <SidebarItem
        @hover="hover.admin = true"
        @leave="hover.admin = false"
        @click="currentPanel == 'Admin' ? setCurrentPanel('None') : setCurrentPanel('Admin')"
        v-if="userAdmin"
      >
        <Admin
          :color="hover.admin ? 'var(--ac)' : 'var(--white)'"
          height="32px"
          width="32px"
          class="icon-tran"
          content="Admin Panel"
          v-tippy
        />
      </SidebarItem>
      <SidebarItem
        @hover="hover.experiments = true"
        @leave="hover.experiments = false"
        @click="
          currentPanel == 'Experiments' ? setCurrentPanel('None') : setCurrentPanel('Experiments')
        "
        v-if="showExperiments"
      >
        <Experiments
          :color="hover.experiments ? 'var(--ac)' : 'var(--white)'"
          height="32px"
          width="32px"
          class="icon-tran"
          content="Experimental Features !! UNSTABLE !!"
          v-tippy
        />
      </SidebarItem>
      <SidebarItem v-if="release" @click="downloadRelease(true)">
        <BringOutAni>
          <Download
            color="var(--green)"
            class="icon-tran"
            height="28px"
            width="28px"
            content="Update Available"
            v-tippy
          />
        </BringOutAni>
      </SidebarItem>

      <SidebarItem
        @hover="hover.settings = true"
        @leave="hover.settings = false"
        @click="currentPanel == 'Settings' ? setCurrentPanel('None') : setCurrentPanel('Settings')"
      >
        <Settings
          :color="hover.settings ? 'var(--ac)' : 'var(--white)'"
          class="icon-tran"
          height="28px"
          width="28px"
          content="Settings"
          v-tippy
        />
      </SidebarItem>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent, computed} from 'vue';
import SelfItem from './SelfItem.vue';
import Friend from '@/components/Icons/Friend.vue';
import SidebarItem from './SidebarItem.vue';
import Group from '@/components/Icons/Group.vue';
// @ts-ignore
import Settings from '@/components/Icons/Settings.vue';
import Admin from '../Icons/Admin.vue';
import {AppStore} from '@/stores/AppStore';
import {ClientStore} from '@/stores/ClientStore';
import Download from '../Icons/Download.vue';
import BringOutAni from '../layout/SideTab/_extensions/BringOutAni.vue';
import Experiments from '@/components/Icons/Experiments.vue';
export default defineComponent({
  name: 'sidebar',
  setup() {
    let appStore = AppStore();
    return {
      currentPanel: computed(() => appStore.currentPanelAct),
      setCurrentPanel: appStore.setCurrentPanel,
      // @ts-ignore
      userAdmin: computed(() => ClientStore().client.self.admin),
      showExperiments: computed(() => appStore.showExperiments),
      downloadRelease: appStore.setDownloadingModalVis,
      release: computed(() => appStore.update),
    };
  },
  data() {
    return {
      hover: {
        friend: false,
        group: false,
        settings: false,
        admin: false,
        experiments: false,
      },
    };
  },
  components: {SelfItem, Friend, SidebarItem, Group, Settings, Admin, Download, BringOutAni, Experiments},
});
</script>

<style lang="scss" scoped>
.sidebar {
  display: flex;
  flex-direction: column;
  max-width: 55px;
  width: 55px;
  min-width: 55px;
  background-color: var(--titlebar);
  max-height: 100vh;
  height: 100vh;
  overflow-y: scroll;
  align-items: center;
  .bottom {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-top: auto;
    margin-bottom: 25px;
    width: 100%;
    flex-direction: column;
  }
  &::-webkit-scrollbar {
    display: none;
  }
}
.icon-tran {
  transition: all 150ms ease;
}
</style>
