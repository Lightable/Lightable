<template>
  <div group="window-decorations"
    class="window-decorations"
    data-tauri-drag-region>
    <div class="titlebar-left"  data-tauri-drag-region/>
    <div class="titlebar-right"  data-tauri-drag-region>
      <div class="titlebar" data-tauri-drag-region>
        <div class="titlebar-button"
          id="titlebar-minimize"
          @click="minimize">
          
        </div>
        <div class="titlebar-button"
          id="titlebar-maximize"
          @click="maximize">
          
        </div>
        <div class="titlebar-button"
          id="titlebar-close"
          @click="close">
          
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
  height: 25px;
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
    width: 4px;
    height: 4px;
    border-radius: 50%;
    color: var(--white);
    background-color: var(--bg);
    transition: all 120ms ease-in-out;
    text-align: center;
    background-color: var(--titlebar);
    padding: 4px;
    &#titlebar-maximize {
      background-color: var(--primaryDefault);
    }
    &#titlebar-minimize {
      background-color: var(--warningDefault);
    }
    &#titlebar-close {
      background-color: var(--errorDefault);
      margin-right: 8px;
    }
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
