<script setup lang="ts">
import { NElement } from 'naive-ui';
</script>

<template>
    <NElement class="pane-wrap">
        <slot name="default" />
    </NElement>
</template>

<style lang="scss" scoped>
.pane-wrap {
    display: flex;
    flex-direction: column;
}

@media only screen and(max-width: 852px) {
    .pane-wrap {
        width: 100%;
        justify-content: center;
        align-items: center;
        margin-bottom: 2%;
    }
}
</style>