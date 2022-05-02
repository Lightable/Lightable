<template>
  <div class="sidebar">
    <SelfItem/>
    <SidebarItem @hover="hover.friend = true" @leave="hover.friend = false" @click="(currentPanel == 'Friends') ? setCurrentPanel('None') : setCurrentPanel('Friends')">
      <Friend :color="(hover.friend) ? 'var(--ac)' : 'var(--white)'" class="icon-tran" height="28px" width="28px"/>
    </SidebarItem>
    <SidebarItem @click="(currentPanel == 'Groups') ? setCurrentPanel('None') : setCurrentPanel('Groups')" type="disabled">
      <Group color="var(--gray)" class="icon-tran" height="28px" width="28px"/>
    </SidebarItem>
    <div class="bottom">

     <SidebarItem @hover="hover.admin = true" @leave="hover.admin = false" @click="(currentPanel == 'Admin') ? setCurrentPanel('None') : setCurrentPanel('Admin')" v-if="userAdmin">
       <Admin :color="(hover.admin) ? 'var(--ac)' : 'var(--white)'" height="32px" width="32px" class="icon-tran"/>
     </SidebarItem> 
    <SidebarItem @hover="hover.settings = true" @leave="hover.settings = false" @click="(currentPanel == 'Settings') ? setCurrentPanel('None') : setCurrentPanel('Settings')">
      <Settings :color="(hover.settings) ? 'var(--ac)' : 'var(--white)'" class="icon-tran" height="28px" width="28px"/>
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
import { AppStore } from '@/stores/AppStore';
import { ClientStore } from '@/stores/ClientStore';
export default defineComponent({
    name: "sidebar",
    setup() {
        let appStore = AppStore();
        return {
          currentPanel: computed(() => appStore.currentPanelAct),
          setCurrentPanel: appStore.setCurrentPanel,
          // @ts-ignore
          userAdmin: computed(() => ClientStore().client.self.admin)
        }
     },
    data() {
      return {
        hover: {
          friend: false,
          group: false,
          settings: false,
          admin: false
        },
        
      }
    },
    components: { SelfItem, Friend, SidebarItem, Group, Settings, Admin }
});
</script>

<style lang="scss" scoped>
.sidebar {
  display: flex;
  flex-direction: column;
  max-width: 55px;
  width: 55px;
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
