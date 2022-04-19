<template>
  <div class="temp-avatar">
    <div class="icon">
      <slot name="icon" />
    </div>
    <div class="status-circle" :status="status" />
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue';

export default defineComponent({
  name: 'Temp-Avatar',
  props: {
    status: Number,
  },
});
</script>

<style lang="scss" scoped>
.temp-avatar {
  position: relative;
  .status-circle {
    position: absolute;
    background-color: var(--gray);
    height: 12px;
    width: 12px;
    margin-bottom: 2px;
    bottom: 0;
    margin-right: -2px;
    border: var(--titlebar) 3px solid;
    right: 0;
    border-radius: 50%;
    &[status='0'] {
      background-color: var(--gray);
    }
    &[status='1'] {
      background-color: var(--yellow);
    }
    &[status='2'] {
      background-color: var(--green);
    }
  }
}
</style>
