<template>
  <BaseModal @click="showModal(false)">
    <template v-slot:icon>
      <ObiWanKenobi color="var(--blue)" height="45" width="45" />
    </template>
    <template v-slot:name>
      <span class="title">These aren't the pages you are looking for</span>
    </template>
    <template v-slot:body>
      <span class="qotd">
        {{ quote }}
      </span>
    </template>
    <template v-slot:actions>
        <button class="move db" dbt="danger" @click="showModal(false)">
            Hmm...
        </button>
    </template>
  </BaseModal>
</template>

<script lang="ts">
import ObiWanKenobi from '@/components/Icons/ObiWanKenobi.vue';
import {defineComponent} from 'vue';
import BaseModal from './BaseModal.vue';
import arrRand from '@/lib/utils/arrRand';
import {JediQuotes} from '@/lib/Constants';
import { AppStore } from '@/stores/AppStore';
export default defineComponent({
  name: 'NotFoundModal',
  components: {BaseModal, ObiWanKenobi},
  setup() {
    return {
      quote: arrRand(JediQuotes),
      showModal: AppStore().showNotFoundModal
    };
  },
});
</script>

<style lang="scss" scoped>
.title {
  font-size: calc(var(--font-size) + 12px);
  margin-left: 8px;
  color: var(--white);
  font-weight: bold;
}
.qotd {
  padding-top: 8px;
  padding-bottom: 8px;
  margin: 10px;
  color: var(--text);
}
.move {
    margin-left: auto;
}
</style>