<template>
  <div class="developer-card">
      <span class="title">&gt;&gt; Developer Options &lt;&lt;</span>
      <div class="subc">
          <tippy content="Shows request data and potentional errors">
          <span class="descript">Debug Mode</span> </tippy> <div class="toggle">
            <Toggle v-model="debugMode" @change="() => { setDebug(debugMode); createSnack('success', (debugMode) ? 'Debug mode is enabled' : 'Debug mode is disabled', true, 5000)}"/>
          </div>
      </div>
      <div class="subc">
           <tippy content="Open Developer Tools">
          <span class="descript gray" @click="openDevtools('open_dev_tools')" type="button">Open Devtools</span> </tippy>
      </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed } from "vue";
import Toggle from '@vueform/toggle';
import { AppStore } from "@/stores/AppStore";
import { SnackStore } from "@/stores/SnackStore";
import { invoke } from "@tauri-apps/api/tauri";
export default defineComponent({
    name: 'DeveloperCard',
    components: {Toggle},
    setup() {
        let appStore = AppStore();
        return {
            debug: computed(() => appStore.debug),
            setDebug: appStore.setDebug,
            createSnack: SnackStore().create,
            openDevtools: invoke
        }
    },
    mounted() {
        this.debugMode = this.debug;
    },
    data() {
        return {
            debugMode: false as boolean
        }
    }
});
</script>

<style lang="scss" scoped>
.developer-card {
    .title {
        color: var(--blue);
        text-align: center;
    }
    .subc {
        display: flex;
        flex-direction: row;
        margin-top: 10px;
        .descript {
            color: var(--white);
           &.gray {
               color: var(--gray);
           }
        }
         .toggle {
                margin-left: auto;
                margin-right: 20px;
            }
    }
}
</style>
