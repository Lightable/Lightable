<script setup lang="ts">

const props = defineProps({
    title: String
})
</script>

<template>
    <div class="section ns" aria-role="section">
        <span class="section-header">
            {{ title }}
        </span>
        <div class="divider" />
        <div class="body">
            <slot/>
        </div>
    </div>
</template>

<style lang="scss" scoped>
.section {
    display: flex;
    flex-direction: column;
    gap: 4px;
    width: 100%;
    margin: 15px;
    .section-header {
        color: var(--text-color-3);
        font-family: 'Sarabun';
        font-weight: 600;
        font-size: 12px;
        font-kerning: 0;
        letter-spacing: .5px;
        margin-left: 2px;
    }
    .divider {
        height: 2px;
        width: 100%;
        background-color: #1D1D23;
        border-radius: .25rem;
        margin-top: 2px;
    }
    .body {
        display: flex;
        flex-direction: column;
        gap: 8px;
        margin-top: 20px;
    }
}
</style>