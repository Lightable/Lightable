<script setup lang="ts">
import { NAvatar } from 'naive-ui';

const props = defineProps({
    title: String 
});
</script>
<template>
    <div class="chat-header">

        <div class="inner-header">
            <slot name="icon"/>
            <span>{{title}}</span>
        </div>
        <div class="right-actions">

        </div>
    </div>
</template>

<style lang="scss" scoped>
.chat-header {
    position: fixed;
    display: flex;
    flex-direction: row;
    padding: 8px;
    // background-color: rgba(9, 9, 9, 0.253);
    backdrop-filter: blur(2px) saturate(200%);
    width: calc(100% - 25px);
    z-index: 5;
    .inner-header {
        font-size: 24px;
        font-weight: bold;
        display: flex;
        flex-direction: row;
        justify-content: center;
        align-items: center;
        gap: 16px;
        color: var(--text-color-1);
    }
}
</style>