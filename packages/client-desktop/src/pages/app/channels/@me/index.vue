<template>
  <div class="home">
    <SecondarySidebar v-if="false"/>
  </div>
</template>


<script lang="ts">
import { defineComponent } from "vue";
import SecondarySidebar from "@/components/app/me/SecondarySidebar/SecondarySidebar.vue";

export default defineComponent({
    components: { SecondarySidebar }
});
</script>

<style lang="scss" scoped>

</style>