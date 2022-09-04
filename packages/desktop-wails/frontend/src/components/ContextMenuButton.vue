<script setup lang="ts">

import { NIcon, NSpin } from 'naive-ui';

const props = defineProps({
    type: String,
    loading: Boolean,
})
</script>

<template>
    <button class="menu-btn" @click="$emit('click')" :t="type">
        <NIcon :size="20" class="icon" v-if="!loading">
            <slot name="icon" />
        </NIcon>
        <NSpin :size="20" :stroke="(type === 'danger' ? 'var(--error-color-pressed)' : 'var(--text-color-3)')" v-if="loading" class="spinner icon" />
        <slot />
    </button>
</template>


<style lang="scss" scoped>
.menu-btn {
    all: unset;
    height: 28px;
    width: 95%;
    padding: 4px;
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: flex-start;
    gap: 12px;
    transition: background 200ms ease;
    color: var(--text-color-2);
    border-radius: 5px;
    cursor: pointer;
    transition: all 250ms ease;
    backface-visibility: hidden;
    -webkit-font-smoothing: subpixel-antialiased;
    &[t='danger'] {
        color: var(--error-color-pressed);
    }

    .icon {
        margin-left: 5px;
    }

    &:hover {
        background-color: #222225;
        transform: scale(1.03);
    }
}
</style>