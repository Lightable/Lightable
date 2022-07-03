<template>
  <div class="sidebar">
    <SelfItem />
    <SidebarItem @click="currentPanel == 'Friends' ? setCurrentPanel('None') : setCurrentPanel('Friends')">
      <template v-slot:text>
        Friends
      </template>
      <template v-slot:icon>
        <Friend color="var(--text)" class="icon-tran" height="22px" width="22px" content="Friends" v-tippy />
      </template>
    </SidebarItem>
    <SidebarItem @click="currentPanel == 'Groups' ? setCurrentPanel('None') : setCurrentPanel('Groups')"
      type="disabled">
      <template #text>
        Groups
      </template>
      <template #icon>
        <Group color="var(--gray)" class="icon-tran" height="22px" width="22px" content="Currently Disabled" v-tippy />
      </template>

    </SidebarItem>
    <div class="bottom">
      <SidebarItem @click="currentPanel == 'Admin' ? setCurrentPanel('None') : setCurrentPanel('Admin')"
        v-if="userAdmin">
        <template #icon>
          <Admin color="var(--warningDefault)" height="22px" width="22px" class="icon-tran" content="Admin Panel"
            v-tippy />
        </template>
        <template #text>
          Admin
        </template>
      </SidebarItem>
      <SidebarItem @click="
        currentPanel == 'Experiments' ? setCurrentPanel('None') : setCurrentPanel('Experiments')
      " v-if="showExperiments">
        <template #icon>
          <Experiments color="var(--primaryDefault)" height="22px" width="22px" class="icon-tran"
            content="Experimental Features !! UNSTABLE !!" v-tippy />
        </template>
        <template #text>
          Experiments
        </template>
      </SidebarItem>
      <SidebarItem v-if="release" @click="downloadRelease(true)">
        <template #icon>
          <BringOutAni>
            <Download color="var(--green)" class="icon-tran" height="22px" width="22px" content="Update Available"
              v-tippy />
          </BringOutAni>
        </template>
        <template #text>
          Update
        </template>
      </SidebarItem>

      <SidebarItem @click="currentPanel == 'Settings' ? setCurrentPanel('None') : setCurrentPanel('Settings')">
        <template #icon>
          <Settings color="var(--text)" class="icon-tran" height="22px" width="22px" content="Settings" v-tippy />
        </template>
        <template #text>
          Settings
        </template>
      </SidebarItem>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue';
import SelfItem from './SelfItem.vue';
import Friend from '@/components/Icons/Friend.vue';
import SidebarItem from './SidebarItem.vue';
import Group from '@/components/Icons/Group.vue';
// @ts-ignore
import Settings from '@/components/Icons/Settings.vue';
import Admin from '../Icons/Admin.vue';
import { AppStore } from '@/stores/AppStore';
import { ClientStore } from '@/stores/ClientStore';
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
    };
  },
  components: { SelfItem, Friend, SidebarItem, Group, Settings, Admin, Download, BringOutAni, Experiments },
});
</script>

<style lang="scss" scoped>
.sidebar {
  display: flex;
  flex-direction: column;
  width: 12rem;
  min-width: 12rem;
  max-width: 12rem;
  background-color: var(--titlebar);
  max-height: 100vh;
  height: 100vh;
  overflow-y: scroll;
  align-items: center;
  padding-left: 0.625rem;
  padding-right: 0.625rem;

  .bottom {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-top: auto;
    margin-bottom: 2rem;
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
