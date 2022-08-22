<script setup lang="ts">
import { computed } from 'vue';
import { useSettingsStore } from '../../stores/SettingsStore';
import { NDrawer, NDrawerContent, NButton, NIcon, NElement } from 'naive-ui';
const settingsStore = useSettingsStore();

const currentPane = computed(() => settingsStore.openPane);
</script>

<template>
    <div class="active-pane" v-if="currentPane === 'None'">
        <NDrawer placement="right" :show="false" :show-mask="false">
            <NDrawerContent title="Settings">
                <div class="body">
                    <section class="section">
                        <span class="header ns">Customization</span>
                        <NButton type="info" tertiary size="large">
                            Customize
                        </NButton>
                    </section>
                    <section class="section">
                        <span class="header ns">Debug</span>
                        <NButton type="primary" tertiary size="large">
                            Debug
                        </NButton>
                    </section>
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