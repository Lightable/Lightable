<template>
  <BaseModal @clicked="$emit('close')">
    <template v-slot:icon>
      <Download color="var(--purple)" />
    </template>
    <template v-slot:name>
      <span class="title">{{(updateFinish) ? '<b class="__green">Finished Downloading</b>' : 'Downloading'}} {{ release?.version }}</span>
    </template>
    <template v-slot:body>
      <h3 class="__title">
        <span class="__version">{{ release?.version }}</span> {{ release?.title }}
      </h3>
      <div class="__description-wrap">
        <span class="__description">
          {{ release?.notes }}
        </span>
      </div>
      <div class="lower-details">
        <span class="__sig">
          {{ release?.signature }}
        </span>
        <span class="__total"> {{ complete }}/{{ total }}</span>
      </div>
    </template>
    <template v-slot:actions>
      <div class="__row" v-if="updateFinish">
      <button class="db" dbt="success" @click="restart()">Restart Now</button>
      <button class="db" dbt="danger" @click="modalVis(false)">I'll Do It Later</button>
      </div>
    </template>
  </BaseModal>
</template>

<script lang="ts">
import {defineComponent, computed} from 'vue';
import BaseModal from './BaseModal.vue';
import Download from '@/components/Icons/Download.vue';
import {checkUpdate, installUpdate} from '@tauri-apps/api/updater';
import {AppStore} from '@/stores/AppStore';
import { relaunch } from '@tauri-apps/api/process';

export default defineComponent({
  name: 'UpdateModal',
  components: {BaseModal, Download},
  setup() {
    let appStore = AppStore();
    return {
      release: computed(() => appStore.update),
      restart: relaunch,
      modalVis: appStore.setNewReleaseModalVis
    };
  },
  async mounted() {
    let {shouldUpdate, manifest} = await checkUpdate();
    if (shouldUpdate) {
      await installUpdate();
      this.updateFinish = true;
    }
  },
  data() {
    return {
      total: 'Unknown',
      complete: 'Unknown',
      updateFinish: false
    };
  },
});
</script>

<style lang="scss" scoped>
@import '@/assets/sass/ui/input.scss';
@import '@/assets/sass/ui/button.scss';
.title {
  font-size: calc(var(--font-size) + 12px);
  margin-left: 8px;
  color: var(--white);
  font-weight: bold;
}
.__title {
  color: var(--text);
  .__version {
    color: var(--purple);
    font-size: 1.2em;
  }
}
.__description-wrap {
  min-height: 150px;
  max-height: 250px;
  background: var(--titlebar);
  border-radius: 8px;
  padding: 4px;
  color: var(--text);
}
.lower-details {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  .__sig {
    font-size: 12px;
    color: var(--blue);
    padding: 4px;
  }
  .__total {
    margin-left: auto;
    color: var(--yellow);
  }
}
.__row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}
.code {
  overflow: scroll;
}
.__green {
  color: var(--green);
}
</style>
