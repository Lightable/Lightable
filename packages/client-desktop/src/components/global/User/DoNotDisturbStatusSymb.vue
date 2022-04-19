<template>
  <StatusSymb color="var(--red)" />
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import StatusSymb from './StatusSymb.vue';
export default defineComponent({
  name: 'DoNotDisturbStatus',
  components: {
    StatusSymb,
  },
});
</script>