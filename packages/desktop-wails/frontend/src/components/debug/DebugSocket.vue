<script setup lang="ts">
import { computed } from 'vue';
import JSONHighlight from '../../composable/JSONHighlight';
import { useAppStore } from '../../stores/AppStore';
import { NDrawer, NDrawerContent, NButton, NIcon, NElement } from 'naive-ui';
import { FileStorage } from '@vicons/carbon';
const appStore = useAppStore();
let websocketDrawer = computed(() => appStore.drawers.websocket);
const data = computed(() => appStore.history.websocket);

const updateWSDrawer = (v: any) => {
    appStore.drawers.websocket = false;
}

const highlight = (s: string) => {
    return JSONHighlight(s);
}
</script>


<template>
    <NDrawer placment="right" @mask-click="updateWSDrawer" :show="websocketDrawer" :default-width="500">
        <NDrawerContent title="Socket Debug" :native-scrollbar="false">
            <div class="body">
                <div class="item" v-if="data.length == 0">
                    <h2>Socket is not connected at this time</h2>
                </div>
                <div class="item" v-for="(item, index) in data" v-bind:key="index">
                    <NElement class="type ns" tag="span">Receive</NElement>
                    <code>
                        <pre v-html="highlight(JSON.stringify(JSON.parse(item), null, 2))"></pre>
                    </code>
                </div>
            </div>
            <div class="action">
                <NButton type="info" text disabled>
                    <template #icon>
                        <FileStorage/>
                    </template>
                    Save To File
                </NButton>
            </div>
        </NDrawerContent>
    </NDrawer>
</template>

<style lang="scss" scoped>
.type {
    color: var(--text-color-3);
}
</style>