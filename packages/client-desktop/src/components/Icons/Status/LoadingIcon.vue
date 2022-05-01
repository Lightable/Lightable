<template>
  <svg
    xmlns="http://www.w3.org/2000/svg"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    viewBox="0 0 100 100"
    preserveAspectRatio="xMidYMid"
    class="loading-icon"
  >
    <g>
      <path d="M50 15A35 35 0 1 0 74.74873734152916 25.251262658470843" stroke-width="12"></path>
      <path d="M49 3L49 27L61 15L49 3" class="loading-arrow"></path>
      <animateTransform
        attributeName="transform"
        type="rotate"
        repeatCount="indefinite"
        dur="1s"
        values="0 50 50;360 50 50"
        keyTimes="0;1"
      ></animateTransform>
    </g>
  </svg>
</template>
<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'LoadingIcon'
})
</script>
<style lang="scss" scoped>
.loading-icon {
  stroke: var(--blue);
  fill: transparent;
  .loading-arrow {
    fill: var(--blue);
  }
}
</style>
