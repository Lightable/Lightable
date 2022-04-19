<template>
  <BaseModal @clicked="$emit('close')">
    <template v-slot:icon>
      <CloudOffline height="32px" width="32px" color="var(--red)"/>
    </template>
    <template v-slot:name>
      <span class="title">You have been disconnected</span>
    </template>
    <!-- <template v-slot:body>
       <h3 style="text-align: center; color: var(--yellow);">Attempting to reconnect you shortly</h3>
    </template> -->
  </BaseModal>
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import BaseModal from './BaseModal.vue';
import CloudOffline from '@/components/global/Icons/CloudOffline.vue';
export default defineComponent({
  name: 'OfflineModal',
  components: {BaseModal, CloudOffline},
  mounted() {
    
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
.code {
  overflow: scroll;
}
</style>