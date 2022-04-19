<template>
    <div class="theme-card">
        <span class="title">Select A Theme</span>
         <CustomSelect :options="['dark', 'fruit', 'liberty', 'moon', 'new', 'ocean']" :default="theme" class="theme-select" @input="(selected) => setTheme(selected as string)"/>
    </div>
</template>

<script lang="ts">

import { defineComponent, computed } from "vue";
import CustomSelect from '@/components/global/CustomSelect.vue';
import { SettingsStore } from "@/stores/SettingsStore";
export default defineComponent({
    name: 'ThemeCard',
    components: {CustomSelect},
    setup() {
        let settingsStore = SettingsStore();
        return {
            setTheme: settingsStore.setCurrentTheme,
            theme: computed(() => settingsStore.themes.current)
        }
    }
});
</script>

<style lang="scss" scoped>
.theme-card {
    display: flex;
    justify-content: center;
    flex-direction: column;
    .title {
        color: var(--purple);
    }
    .theme-select {
        width: 80%;
        margin-top: 10px;
    }
}
</style>