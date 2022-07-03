<template>
  <div group="window-decorations"
    class="window-decorations"
    data-tauri-drag-region>
    <div class="titlebar-left"
      data-tauri-drag-region />
    <div class="titlebar-right"
      data-tauri-drag-region>
      <div class="titlebar"
        data-tauri-drag-region>
        <div class="titlebar-button"
          id="titlebar-minimize"
          @click="minimize">
          <svg xmlns="http://www.w3.org/2000/svg"
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round">
            <line x1="5"
              y1="12"
              x2="19"
              y2="12" />
          </svg>
        </div>
        <div class="titlebar-button"
          id="titlebar-maximize"
          @click="maximize">
          <svg width="16px"
            height="16px"
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg">
            <path d="M7 4H4V7"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
              stroke-linejoin="round" />
            <path d="M17 4H20V7"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
              stroke-linejoin="round" />
            <path d="M7 20H4V17"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
              stroke-linejoin="round" />
            <path d="M17 20H20V17"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
              stroke-linejoin="round" />
          </svg>
        </div>
        <div class="titlebar-button"
          id="titlebar-close"
          @click="close">
          <svg xmlns="http://www.w3.org/2000/svg"
            height="18px"
            viewBox="0 0 24 24"
            width="18px"
            fill="currentColor">
            <path d="M0 0h24v24H0V0z"
              fill="none" />
            <path d="M18.3 5.71c-.39-.39-1.02-.39-1.41 0L12 10.59 7.11 5.7c-.39-.39-1.02-.39-1.41 0-.39.39-.39 1.02 0 1.41L10.59 12 5.7 16.89c-.39.39-.39 1.02 0 1.41.39.39 1.02.39 1.41 0L12 13.41l4.89 4.89c.39.39 1.02.39 1.41 0 .39-.39.39-1.02 0-1.41L13.41 12l4.89-4.89c.38-.38.38-1.02 0-1.4z" />
          </svg>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent } from 'vue';
import { appWindow } from '@tauri-apps/api/window';
import Logo from '@/components/Icons/Logo.vue';
import { AppStore } from '@/stores/AppStore';
export default defineComponent({
  name: 'AppTitleBar',
  setup() {
    let appStore = AppStore();
    return { titleDetails: computed(() => appStore.titleDetails) };
  },
  mounted() {
    appWindow.setTitle(`ZenSpace • ${this.titleDetails}`);
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
  components: { Logo },
});
</script>
<style lang="scss" scoped>
.titlebar-left {
  width: 13.25rem;
  min-width: 13.25rem;
  max-width: 13.25rem;
}

.titlebar-right {
  width: 100%;
  background-color: var(--neturalDefaultBase);
}

.window-decorations {
  display: flex;
  flex-direction: row;
  position: relative;
  z-index: 2;
  height: 29px;
  background-color: var(--titlebar);
  z-index: 200;
  width: 100%;
  // background-color: var(--neturalDefaultBase);

  .titlebar {
    height: 25px;
    user-select: none;
    display: flex;
    justify-content: flex-end;
    margin-left: auto;
    align-items: center;
    gap: 4px;
  }

  .titlebar-button {
    display: inline-flex;
    justify-content: center;
    align-items: center;
    width: 32px;
    height: 24px;
    color: var(--white);
    transition: all 120ms ease-in-out;
    text-align: center;
    padding: 4px;
    svg {
      margin-top: 3px;
    }

    &:hover {
      background-color: var(--titlebar-hover);
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
    width: 12rem;

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
