<script setup lang="ts">
import { useAppStore } from '../../../stores/AppStore';
import AppHeader from '../../../components/header/AppHeader.vue';

import SettingsHeader from '../../../components/settings/SettingsHeader.vue';
const appStore = useAppStore();
appStore.setTitle('Settings');
</script>


<template>
    <SettingsHeader />
    <div class="settings-page">
        <router-view />
    </div>
</template>


<style lang="scss" scoped>
.settings-page {
    width: 100%;
    height: 100%;
    overflow-y: auto;
    &::-webkit-scrollbar {
        display: none;
    }
}
</style>