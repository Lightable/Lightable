<script setup lang="ts">

const props = defineProps({
    title: String,
    data: String
})
</script>


<template>
    <div class="stat-card">
        <slot />
        <span class="stat-header">{{ title }}</span>
        <div class="divider" />
        <span class="stat-data">{{(data !== 'undefined' || !data) ? data : '?' }}</span>
    </div>
</template>


<style lang="scss" scoped>
.stat-card {
    display: flex;
    flex-direction: row;
    gap: 30px;
    align-items: center;
    height: 28px;
    background-color: #9b9da410;
    padding: 10px;
    border-radius: .5rem;
    max-width: 65%;
    min-width: 10%;
    .stat-header {
        font-size: 22px;
        font-weight: bold;
        width: 190px;
        color: var(--text-color-1);
    }

    .divider {
        height: 32px;
        width: 3px;
        border-radius: .25rem;
        background-color: #202027;
    }
    .stat-data {
        font-size: 22px;
        color: var(--text-color-3);
        font-weight: bold;
    }
}
</style>

