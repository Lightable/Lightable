<script setup lang="ts">
import { computed } from 'vue';
import { useSettingsStore } from '../../stores/SettingsStore';
import { NDrawer, NDrawerContent, NButton, NIcon, NElement } from 'naive-ui';
const settingsStore = useSettingsStore();

const currentPane = computed(() => settingsStore.openPane);
</script>

<template>
    <div class="active-pane">
        <NDrawer placement="right" :show="true" :show-mask="true" v-if="currentPane === 'Customization'">
            <NDrawerContent title="Customize">
                <div class="body">
                    
                </div>
            </NDrawerContent>
        </NDrawer>
    </div>
    <slot/>
</template>

<style lang="scss" scoped>
.body {
    display: flex;
    flex-direction: column;
    gap: 8px;
    .section {
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        gap: 8px;
        .header {
            font-size: 18px;
        }
    }
}
</style>