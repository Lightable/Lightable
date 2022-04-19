<template>
 <div class="bring-out" v-motion
    :initial="{
      opacity: 0,
      x: -50
    }"
    :enter="{
      opacity: 1,
      x: 0,
    }">
    <slot/>
 </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

    export default defineComponent({
        name: 'Unravel'
    });
</script>