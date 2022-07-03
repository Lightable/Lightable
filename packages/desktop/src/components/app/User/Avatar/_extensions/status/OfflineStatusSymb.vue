<template>
  <StatusSymb color="var(--gray)" />
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import StatusSymb from './_base/StatusSymb.vue';
export default defineComponent({
  name: 'OfflineStatus',
  components: {
    StatusSymb,
  },
});
</script>