<template>
  <svg xmlns="http://www.w3.org/2000/svg" class="icon" viewBox="0 0 24 24" :fill="color">
    <path d="M0 0h24v24H0V0z" fill="none" />
    <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z" />
  </svg>
</template>

<script lang="ts">
import {defineComponent} from 'vue';

export default defineComponent({
  name: 'LogoIcon',
  props: {
    color: String,
  },
});
</script>
