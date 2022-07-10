<script setup lang="ts">
import { defineAsyncComponent } from "vue";
import { useHead } from "@vueuse/head";
const ZSlogan = defineAsyncComponent({
  loader: () => import('../components/ZSlogan.vue')
});
const ZOptions = defineAsyncComponent({
  loader: () => import('../components/ZOptions.vue')
});
const ZTestimonial = defineAsyncComponent({
  loader: () => import('../components/ZTestimonial.vue')
});
const ZAdminDisplay = defineAsyncComponent({
  loader: () => import('../components/ZAdminDisplay.vue')
});
 useHead({
      title: 'ZenSpace 🡪 Home',
      meta: [
        {
          property: 'og:title',
          content: 'ZenSpace 🡪 Home',
        },
      ]
    });
</script>

<template>
  <div class="main-page">
    <ZSlogan />
    <ZOptions />
    <ZAdminDisplay/>
    <div class="bottom">
      <ZTestimonial />
    </div>
  </div>
</template>

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