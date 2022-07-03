<script setup lang="ts">
import { computed } from 'vue';
import { NCarousel, NElement } from 'naive-ui';
import { useAppStore } from '../stores/AppStore';
let isDark = computed(() => useAppStore().isDark)
</script>

<template>
    <h1>Testimonials</h1>
    <NElement type="div" class="testimonial" :style="(isDark) ? 'background-color: var(--slide-dark);' : 'background-color: var(--slide-light);'">
        <NCarousel show-arrow autoplay :interval="10000">
           <h2>Beautifully shit <h3 style="color: gray;">- Some asshole (probably)</h3></h2>
           <h2>It's nice, if you don't look at the code <h3 style="color: gray;">- Developer themself</h3></h2>
           <h2>Definitely stupid indeed<h3 style="color: gray;">- Some British Teeth</h3></h2>
        </NCarousel>
    </NElement>
</template>

<style lang="scss" scoped>
.testimonial {
    border-radius: 8px;
    padding: 25px;
    display: flex;
    height: 200px;
    width: 75vw;
    h2 {
        --text-color-1: #fff;
        color: var(--text-color-1);
    }
}
@media only screen and (max-width: 1147px) {
 .testimonial {
    height: 140px;
    h2 {
        font-size: 18px;
    }
    h3 {
        font-size: 14px;
    }
 }
}
</style>