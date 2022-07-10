<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
const Element = defineAsyncComponent({
    loader: () => import('naive-ui/lib/element/src/Element')
});
const Spin = defineAsyncComponent({
    loader: () => import('naive-ui/lib/spin/src/Spin')
});
</script>

<template>
    <Element class="profile-page">
        <div class="soon">
            <Spin />
                <span :style="{ color: 'var(--error-color)', fontSize: '42px', userSelect: 'none' }">Coming Soon</span>
            <Spin />
        </div>
        <router-view/>
    </Element>
</template>

<style lang="scss" scoped>
.profile-page {
    display: flex;
    flex-direction: row;
    justify-content: center;
    align-items: center;
    min-height: calc(100vh - 46.4px);
    .soon {
        display: flex;
        flex-direction: row;
        justify-content: center;
        align-items: center;
        gap: 16px;
    }
}
</style>