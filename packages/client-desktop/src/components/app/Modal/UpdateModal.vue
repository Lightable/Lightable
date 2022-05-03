<template>
  <BaseModal @clicked="$emit('close')">
    <template v-slot:icon>
      <Download color="var(--purple)" />
    </template>
    <template v-slot:name>
      <span class="title">Downloading {VERSION}</span>
    </template>
    <template v-slot:body>
      <h3 class="__title"><span class="__version">0.0.1</span> Release Title</h3>
      <div class="__description-wrap">
      <span class="__description">
          This is a release description
      </span>
      </div>
      <span class="__sig">
          ReleaseSig
      </span>
    </template>
  </BaseModal>
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import BaseModal from './BaseModal.vue';
import Download from '@/components/Icons/Download.vue';
import { checkUpdate, installUpdate } from '@tauri-apps/api/updater';
export default defineComponent({
  name: 'UpdateModal',
  components: {BaseModal, Download},
  async mounted() {
      let isUpdate = await checkUpdate().catch(err => console.log(err));
      if (isUpdate.shouldUpdate) {
          console.log('Should Update!');
      }
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
        font-size: 1.20em;
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
.__sig {
    font-size: 12px;
    color: var(--blue);
    padding: 4px;
}
.code {
  overflow: scroll;
}
</style>
