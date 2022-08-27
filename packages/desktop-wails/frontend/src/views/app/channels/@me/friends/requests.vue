<script setup lang="ts">
import AppSection from '../../../../../components/app/AppSection.vue';
import { useAppStore } from '../../../../../stores/AppStore';
const appStore = useAppStore();

appStore.setTitle('Friends • Requests');
</script>

<template>
    <AppSection title="REQUESTS">
        <h3 style="color: var(--text-color-2); text-align: center;" class="center">Could not find any requests...<br>At least you're not left on pending 😎</h3>
    </AppSection>
</template>


<style lang="scss" scoped>
</style>