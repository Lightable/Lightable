<script setup lang="ts">
import Overlay from '../Overlay.vue';
import SearchGlobal from './SearchGlobal.vue';

const props = defineProps({
    show: Boolean
})
</script>

<template>
    <Transition name="overlay-fade">
        <Overlay v-if="show">
            <SearchGlobal />
        </Overlay>
    </Transition>
    <slot />
</template>


<style lang="scss" scoped>
.overlay-fade-enter-active,
.overlay-fade-leave-active {
  transition: opacity .4s ease;
}
.overlay-fade-enter-from,
.overlay-fade-leave-to {
  opacity: 0;
}

</style>