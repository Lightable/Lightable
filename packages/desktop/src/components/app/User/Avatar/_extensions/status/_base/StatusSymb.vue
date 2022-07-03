<template>
  <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" :fill="color">
    <circle cx="12" cy="12" r="12" />
  </svg>
</template>


<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
    name: 'StatusSymb',
    props: {
        color: String
    }
});
</script>