<template>
  <StatusSymb color="var(--green)" />
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import StatusSymb from './StatusSymb.vue';
export default defineComponent({
  name: 'OnlineStatus',
  components: {
    StatusSymb,
  },
});
</script>
