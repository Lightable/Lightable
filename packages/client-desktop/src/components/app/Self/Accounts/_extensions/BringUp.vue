<template>
 <div class="bring-up" v-motion
    :initial="{
      opacity: 0,
      y: '100%'
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
        name: 'BringUp'
    });
</script>