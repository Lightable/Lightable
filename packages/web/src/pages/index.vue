<template>
  <div class="main-page">
    <ZSlogan />
    <ZOptions />
    <div class="bottom">
      <ZTestimonial />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import ZSlogan from "../components/ZSlogan.vue";
import ZOptions from "../components/ZOptions.vue";
import ZTestimonial from "../components/ZTestimonial.vue";
import { useHead } from "@vueuse/head";
export default defineComponent({
  name: 'ZHome',
  components: {
    ZSlogan,
    ZOptions,
    ZTestimonial
  },
  setup() {
    useHead({
      title: 'ZenSpace 🡪 Home',
      meta: [
        {
          property: 'og:title',
          content: 'ZenSpace 🡪 Home',
        },
      ]
    })
  }
});
</script>

<style lang="scss" scoped>
.main-page {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
  height: calc(100vh - 80px);
}

.bottom {
  margin-top: auto;
}
</style>