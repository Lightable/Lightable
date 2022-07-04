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
          name: 'description',
          content: 'ZenSpace is an open source chat app full of mistakes :)'
        },
        {
          property: 'og:title',
          content: 'ZenSpace 🡪 Home',
        },
        {
          property: 'og:site_name',
          content: 'ZenSpace'
        },
        {
          property: 'og:url',
          content: 'https://zenspaceweb.vercel.app/#/'
        }, 
        {
          property: 'og:description',
          content: 'ZenSpace is an open source chat app full of mistakes :)'
        },
        {
          property: 'og:type',
          content: 'website'
        },
        {
          name: 'theme-color',
          content: '#58F2A2'
        },
        {
          property: 'og:image',
          content: 'https://api.zenspace.cf/experimental/image/generate?type=EMBED&text=ZenSpace%20[[0xf061]]%20Home&color=58F2A2'
        },
        {
          name: 'twitter:card',
          content: 'summary_large_image'
        }
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