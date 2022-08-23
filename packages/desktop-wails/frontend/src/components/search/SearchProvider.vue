<script setup lang="ts">
import Overlay from '../Overlay.vue';
import SearchGlobal from './SearchGlobal.vue';

const props = defineProps({
    show: Boolean
})
</script>

<template>
    <Overlay v-if="show">
        <SearchGlobal />
    </Overlay>
    <slot />
</template>