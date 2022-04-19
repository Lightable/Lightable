<template>
    <div class="sidebar-item" @mouseover="$emit('hover')" @mouseleave="$emit('leave')" type="button">
      <slot :color="(hover) ? 'var(--ac)' : 'var(--white)'"/>
    </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue';

export default defineComponent({
  name: 'SidebarItem',
  props: {
    tooltip: String,
  },
  data() {
      return {
        hover: false
      }
    },
});
</script>


<style lang="scss">
:root {
    --titlebar-hover: #202020;
}
.sidebar-item {
    padding: 5px;
    width: 100%;
    height: 42px;
    background-color: var(--titlebar);
    display: flex;
    justify-content: center;
    align-items: center;
    transition: all 150ms ease;
    &:hover {
        background-color: var(--titlebar-hover);
    }
}
</style>
