<script setup lang="ts">
import {NButton} from 'naive-ui';

const props = defineProps({
    title: String,
    data: String,
    action: Object as any,
})
</script>

<template>
    <div class="debug-sec">
        <span class="header">{{title}}</span>
        <div class="data">
            <span class="data" v-if="data">{{data}}</span>
            <slot/>
        </div>
        <div class="action">
            <NButton secondary :type="action.type" @click="$emit('button')">{{action.title}}</NButton>
        </div>
    </div>
</template>


<style lang="scss" scoped>
.debug-sec {
    display: inline-flex;
    flex-direction: column;
    gap: 16px;
    align-items: center;
    max-width: 320px;
    min-width: 250px;
    .header {
        font-size: 24px;
        font-weight: bold;
    }
    .data {
        color: var(--text-color-3);
        font-size: 18px;
        width: 100%;
        text-align: center;
    }
}
</style>