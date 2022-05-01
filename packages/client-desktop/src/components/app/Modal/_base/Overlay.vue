<template>
  <div class="overlay fadeIn">
    <div class="data">
      <slot></slot>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue';

export default defineComponent({
  name: 'Overlay',
});
</script>

<style lang="scss" scoped>
.overlay {
  position: absolute;
  height: 100vh;
  width: 100vw;
  background: rgba(0, 0, 0, 0.486);
  z-index: 10;
  .data {
    height: 100%;
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
  }
}
</style>
