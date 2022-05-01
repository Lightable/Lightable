<template>
  <StatusSymb color="var(--yellow)" />
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import StatusSymb from './_base/StatusSymb.vue';
export default defineComponent({
  name: 'IdleStatus',
  components: {
    StatusSymb,
  },
});
</script>