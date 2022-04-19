<template>
 <div class="unravel" v-motion
    :initial="{
      opacity: 0,
      y: -200
    }"
    :enter="{
      opacity: 1,
      y: 0,
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