<template>
  <BaseModal>
    <template v-slot:icon>
      <Failed height="24px" width="24px" color="var(--error)" />
    </template>
    <template v-slot:name>
      <span class="title">Too Many Connections</span>
    </template>
    <template v-slot:body>
        <Device class="device"/>
    </template>
    <template v-slot:actions>
        <button class="db move" dbt="danger" @click="$router.push('/login');"> 
            Log Out of this Device
        </button>
    </template>
  </BaseModal>
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import BaseModal from './BaseModal.vue';
import Failed from '@/components/Icons/Failed.vue';
import Device from '@/components/global/Device.vue';
export default defineComponent({
  name: 'TooManyConnections',
  components: {
    BaseModal,
    Failed,
    Device
},
});
</script>

<style lang="scss" scoped>
.title {
  font-size: calc(var(--font-size) + 10px);
  margin-left: 8px;
  color: var(--white);
  font-weight: bold;
}
.move {
    margin-left: auto;
}
.device {
    margin: 8px;
}
</style>
