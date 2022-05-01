<template>
  <BaseModal @clicked="$emit('close')">
    <template v-slot:icon>
      <Debug width="30px" height="30px" color="var(--purple)"/>
    </template>
    <template v-slot:name>
      <span class="title">Debug Data</span>
    </template>
    <template v-slot:body>
       <VueJsonPretty :data="{key: true}"></VueJsonPretty>
    </template>
    <template v-slot:actions>
    </template>
  </BaseModal>
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import BaseModal from './BaseModal.vue';
import Debug from '@/components/Icons/Debug.vue';
import parseCode from '@/lib/utils/parseCode';
import VueJsonPretty from 'vue-json-pretty';
import 'vue-json-pretty/lib/styles.css';
export default defineComponent({
  name: 'DebugModal',
  components: {BaseModal, Debug, VueJsonPretty},
  setup() {
    return {
      parseJSON: parseCode,
    }
  },
  mounted() {
    
  },
  props: {
      content: Object
  }
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
