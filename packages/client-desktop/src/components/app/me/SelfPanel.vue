<template>
  <div class="panel">
   
    <div class="details">
      <span class="name">{{me?.name}}</span>
      <!-- <span class="status">{{m}}</span> -->
    </div>
    <div class="actions">
      <Settings color="var(--white)" class="action-btn" @click="$router.push('/app/settings')" />
      <!-- <SourceIcon color="var(--blue)" class="action-btn" @click="openDevTools"/> -->
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent, computed} from 'vue';
import Avatar from '../User/Avatar/Avatar.vue';
import Settings from '@/components/global/Icons/Settings.vue';
import {invoke} from '@tauri-apps/api/tauri';
import {ClientStore} from '@/stores/ClientStore';
// import SourceIcon from '../../global/Icons/SourceIcon.vue';

export default defineComponent({
  name: 'selfpanel',
  components: {Avatar, Settings},
  setup() {
    let self = ClientStore().client?.self;
    return {me: computed(() => self)};
  },
  methods: {
    openDevTools() {
      invoke('open_dev_tools');
    },
    openSelfContext(e: MouseEvent) {
      let ctx = this;
      let client = ClientStore().client;
      e.preventDefault();
      this.$contextmenu({
        x: e.x,
        y: e.y,
        items: [
          {
            label: 'Copy ID',
            onClick() {
              ctx.$copyText(client?.self?._id as string)
            }
          }
        ]
      })
    }
  },
});
</script>

<style lang="scss" scoped>
.panel {
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  align-items: center;
  background: var(--titlebar);
  width: 100%;
  display: flex;
  flex-direction: row;
  // border-top-left-radius: 8px;
  // border-top-right-radius: 8px;
  .details {
    display: flex;
    align-items: flex-start;
    justify-content: left;
    height: 40px;
    flex-direction: column;
    margin-left: 5px;
    .name {
      font-weight: bold;
      color: var(--white);
    }
    .status {
      color: var(--gray);
    }
  }
  .actions {
    margin-left: auto;
    margin-right: 10px;
    .action-btn {
      transition: 150ms ease;
      cursor: pointer;
      &:hover {
        filter: brightness(60%);
      }
    }
  }
}
</style>
