<template>
  <div class="experiments">
    <SideContextMenu>
      <template v-slot:icon>
        <Experiments height="35px"
          width="35px"
          color="var(--white)" />
      </template>
      <template v-slot:header> Experiments </template>
      <template v-slot:body>
        <div class="subc"
          v-for="(kv, index) in experiments"
          v-bind:key="index">
          <tippy :content="kv[0]">
            <span class="descript gray"
              type="button">{{ kv[0] }}</span>
          </tippy>
          <NButton type="primary" v-if="!kv[1]" secondary @click="setExperiment(kv[0], true)">
            <NIcon>
              <EnableIcon />
            </NIcon>
            Enable Experiment
          </NButton>
          <NButton type="error" v-if="kv[1]" secondary @click="setExperiment(kv[0], false)">
          <NIcon>
            <DisableIcon/>
          </NIcon>
            Disable Experiment
          </NButton>
          <!-- <button class="db"
            dbt="success"
            v-if="!kv[1]"
            @click="setExperiment(kv[0], true)">
            Enable Experiment
          </button>
          <button class="db"
            dbt="danger"
            v-if="kv[1]"
            @click="setExperiment(kv[0], false)">
            Disable Experiment
          </button> -->
        </div>
      </template>
    </SideContextMenu>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, defineAsyncComponent } from 'vue';
import SideContextMenu from '../index.vue';
import Experiments from '@/components/Icons/Experiments.vue';
import { AppStore } from '@/stores/AppStore';
import { NIcon } from 'naive-ui/lib';
export default defineComponent({
  name: 'ExperimentContextMenu',
  components: {
    SideContextMenu,
    Experiments,
    NButton: defineAsyncComponent(() => import('naive-ui/lib/button/src/Button')),
    NIcon,
    EnableIcon: defineAsyncComponent(() => import('@vicons/ionicons5/Add')),
    DisableIcon: defineAsyncComponent(() => import('@vicons/ionicons5/Remove'))
  },
  setup() {
    let appStore = AppStore();
    return {
      experiments: computed(() => appStore.experiments),
      setExperiment: appStore.setExperiment,
    };
  },
});
</script>

<style lang="scss" scoped>
.subc {
  display: flex;
  flex-direction: column;
  margin-top: 10px;

  .descript {
    color: var(--white);

    &.gray {
      color: var(--text);
    }
  }

  .toggle {
    margin-left: auto;
    margin-right: 20px;
  }
}
</style>
