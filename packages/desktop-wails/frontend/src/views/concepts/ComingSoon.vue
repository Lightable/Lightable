<script lang="ts" setup>
import { NIcon } from 'naive-ui';
import { Alarm } from '@vicons/carbon';
import { useAppStore } from '../../stores/AppStore';
import { computed } from 'vue';

const appStore = useAppStore();

const version = computed(() => appStore.version);
</script>

<template>
    <div class="soon ns">
        <NIcon color="var(--warning-color)" :size="128">
            <Alarm />
        </NIcon>
        <h1 class="ns">Coming Soon! Stay Tuned</h1>
    </div>
    <div class="app-info ns">
        <span class="app">Lightable Red</span>
        <span class="version">{{version}}</span>
    </div>
</template>


<style lang="scss" scoped>
.soon {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: inherit;
    width: 100%;
    gap: 16px;
    h1 {
        color: var(--text-color-1);
    }
}
.app-info {
    margin-left: 15px;
    display: flex;
    flex-direction: row;
    gap: 8px;
    .app {
        color: var(--lightable-red);
    }
    .version {
        color: var(--text-color-3);
    }
}
</style>