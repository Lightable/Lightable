<script setup lang="ts">
import { NImage, NImageGroup } from 'naive-ui';

const props = defineProps({
    src: String
});
</script>


<template>
    <div class="attachment">
        <NImageGroup>
            <NImage :height="72" :src="src" :preview-src="src" />
            <NImage :height="72" :src="src" :preview-src="src" />
            <NImage :height="72" :src="src" :preview-src="src" />
        </NImageGroup>
    </div>
</template>


<style lang="scss" scoped>
.attachment {
    transition: all 250ms ease;
    cursor: pointer;

    &:hover {
        transform: scale(1.15);
        filter: saturate(110%);
    }
}
</style>
