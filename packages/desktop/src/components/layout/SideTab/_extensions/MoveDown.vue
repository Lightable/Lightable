<template>
 <div class="move-down" v-motion
    :initial="{
      opacity: 0,
      y: -50
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
        name: 'MoveDown'
    });
</script>