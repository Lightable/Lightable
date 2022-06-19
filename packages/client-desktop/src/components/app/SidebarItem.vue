<template>
  <div class="sidebar-item" @mouseover="$emit('hover')" @mouseleave="$emit('leave')" :type="type">

    <slot color="white" name="icon" />
    <span :style="`color: var(--${type == 'disabled' ? 'gray' : 'text'});`" class="text">
      <slot name="text" />
    </span>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'SidebarItem',
  props: {
    tooltip: String,
    type: String
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
  --titlebar-hover: #ffffff09;
}

.sidebar-item {
  border-radius: 4px;
  display: flex;
  height: 16px;
  flex-direction: row;
  align-items: center;
  width: 100%;
  transition: all 150ms ease;
  padding-left: 0.5rem;
  padding-right: 0.5rem;
  padding-top: 0.50rem;
  padding-bottom: 0.50rem;
  margin-top: 2px;
  .text {
    margin-left: 10px;
    font-size: calc(var(--font-size) + 8px);
    font-family: Inter;
  }

  &:hover {
    &[type='disabled'] {
      cursor: not-allowed;
      background-color: transparent;
    } 
    &:not([type]) {
      background-color: var(--primaryAccent);
      cursor: pointer;
    }
    
  }
}
</style>
