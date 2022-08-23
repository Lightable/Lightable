<script setup lang="ts">

const props = defineProps({
    title: String
})
</script>

<template>
    <div class="app-header ns">
        <slot name="icon" />
        <h2>{{title}}</h2>
        <div class="divider"/>
        <div class="actions">
            <slot name="actions"/>
        </div>
    </div>
</template>

<style lang="scss" scoped>
.app-header {
    padding-left: 10px;
    height: 42px;
    gap: 16px;
    display: flex;
    flex-direction: row;
    align-items: center;
    background-color: #1919208f;
    .divider {
        height: 20px;
        width: 3px;
        background-color: rgba(255, 255, 255, 0.068);
        border-radius: .25rem;
    }
}
</style>