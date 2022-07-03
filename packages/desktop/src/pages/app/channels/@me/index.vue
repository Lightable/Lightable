<template>
  <div class="home">
    <router-view/>
  </div>
</template>


<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
    components: { }
});
</script>

<style lang="scss" scoped>
.home {
  height: 100%;
}
</style>