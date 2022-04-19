<template>
  <div group="window-decorations" class="window-decorations" data-tauri-drag-region>
    <span id="titlebar-title" data-tauri-drag-region>
      <Logo color="var(--white)" height="18px" width="18px" data-tauri-drag-region/>
      <div class="name-details" data-tauri-drag-region><span data-tauri-drag-region>Chatty</span><span class="title-details" v-if="titleDetails" data-tauri-drag-region>• {{titleDetails}}</span></div>
    </span>
    <div class="titlebar">
      <div class="titlebar-button" id="titlebar-minimize" @click="minimize">
        <span alt="Minimize"
          ><svg aria-hidden="false" width="12" height="12" viewBox="0 0 12 12">
            <rect fill="currentColor" width="10" height="1" x="1" y="6"></rect></svg></span>
      </div>
      <div class="titlebar-button" id="titlebar-maximize" @click="maximize">
        <span alt="Maximize"
          ><svg aria-hidden="false" width="12" height="12" viewBox="0 0 12 12">
            <rect
              width="9"
              height="9"
              x="1.5"
              y="1.5"
              fill="none"
              stroke="currentColor"
            ></rect></svg></span>
      </div>
      <div class="titlebar-button" id="titlebar-close" @click="close">
        <span alt="close"
          ><svg aria-hidden="false" width="12" height="12" viewBox="0 0 12 12">
            <polygon
              fill="currentColor"
              fill-rule="evenodd"
              points="11 1.576 6.583 6 11 10.424 10.424 11 6 6.583 1.576 11 1 10.424 5.417 6 1 1.576 1.576 1 6 5.417 10.424 1"
            ></polygon></svg></span>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {computed, defineComponent} from 'vue';
import {appWindow} from '@tauri-apps/api/window';
import Logo from '../global/Icons/Logo.vue';
import {AppStore} from '@/stores/AppStore';
export default defineComponent({
  name: 'AppTitleBar',
  setup() {
    let appStore = AppStore();
    return {titleDetails: computed(() => appStore.titleDetails)};
  },
  mounted() {
    appWindow.setTitle(`Chatty • ${this.titleDetails}`);
  },
  methods: {
    minimize() {
      appWindow.minimize();
    },
    maximize() {
      appWindow.toggleMaximize();
    },
    close() {
      appWindow.close();
    },
  },
  components: {Logo},
});
</script>
<style lang="scss" scoped>
.window-decorations {
  display: flex;
  flex-direction: row;
  position: relative;
  z-index: 2;
  height: 25px;
  background-color: var(--titlebar);
  .titlebar {
    height: 25px;
    user-select: none;
    display: flex;
    justify-content: flex-end;
    margin-left: auto;
    align-items: center;
    
  }
  .titlebar-button {
    display: inline-flex;
    justify-content: center;
    align-items: center;
    width: 22px;
    height: 16px;
    color: var(--white);
    background-color: var(--bg);
    transition: all 120ms ease-in-out;
    text-align: center;
    background-color: var(--titlebar);
    padding: 4px;
    svg {
      margin-top: 3px;
    }
    &:hover {
      filter: brightness(120%);
    }
  }
  #titlebar-title {
    display: flex;
    color: var(--white);
    font-weight: normal;
    overflow: hidden;
    user-select: none;
    padding: 2px;
    align-items: center;
    flex-direction: row;
    justify-content: center;
    font-family: Inter;
    font-smooth: always;
    margin-left: 4px;

    .name-details {
      margin-bottom: 2.5px;
      font-size: 13px;
      span {
        margin-left: 3px;
      }
    }
  }

  #titlebar-close:hover {
    background-color: var(--red) !important;
    filter: none !important;
  }
}
</style>
