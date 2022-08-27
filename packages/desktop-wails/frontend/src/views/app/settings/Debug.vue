<script setup lang="ts">
import { ref, computed, Ref, onUnmounted } from 'vue';
import { GetConfig, GetMemoryStats, ForceGC } from '../../../../wailsjs/go/app/App';
import { app } from '../../../../wailsjs/go/models';
import AppSection from '../../../components/app/AppSection.vue';
import JSONHighlight from '../../../composable/JSONHighlight';
import { useAppStore } from '../../../stores/AppStore';
import bytesToReadable from '../../../composable/bytesToReadable';
const appStore = useAppStore();
const data = computed(() => appStore.history.websocket);

let memInt: any;
const highlight = (s: string) => {
    return JSONHighlight(s);
}
const configHTML = ref('Not avaliable');

const memStats = ref(undefined) as Ref<app.CustomMemoryStats | undefined>;
GetConfig().then(c => {
    configHTML.value = highlight(JSON.stringify(c, null, 2))
})

GetMemoryStats().then(s => {
 memStats.value = s;
    memInt = setInterval(async () => {
        memStats.value = await GetMemoryStats()
    }, 1000)
})


onUnmounted(() => {
    clearInterval(memInt)
})
</script>

<template>
    <AppSection title="FRONTEND">
        Not avaliable
    </AppSection>
    <AppSection title="MEMORY">
        <div class="expanable-row">
             <div class="memory-stats" v-if="memStats">
                    <span class="memory-item">
                        Allocated: <b class="mem-value">{{bytesToReadable(memStats.alloc)}}</b>
                    </span>
                    <span class="memory-item">
                        Total GC: <b class="mem-value">{{memStats.gcTotal}}</b>
                    </span>
                    <span class="memory-item">
                        HeapSpace: <b class="mem-value">{{bytesToReadable(memStats.heapSpace)}}</b>
                    </span>
                    <span class="memory-item">
                        System Allocation: <b class="mem-value">{{bytesToReadable(memStats.sysAlloc)}}</b>
                    </span>
                    <span class="memory-item">
                        Total Allocation: <b class="mem-value">{{bytesToReadable(memStats.totalAlloc)}}</b>
                    </span>

                </div>
        </div>
    </AppSection>
    <AppSection title="SOCKET LOGS">
        <div class="item" v-for="(item, index) in data" v-bind:key="index">
            <code>
                        <pre v-html="highlight(JSON.stringify(JSON.parse(item), null, 2))"></pre>
            </code>
        </div>
    </AppSection>
    <AppSection title="CONFIG">
        <code>
           <pre v-html="configHTML"></pre>
        </code>
    </AppSection>
</template>


<style lang="scss" scoped>
.expandable-row {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;

}

.debug-sec {
    margin-top: 20px;
}

.memory-stats {
    display: flex;
    width: 100%;
    flex-direction: column;
    justify-content: flex-start;
    text-align: left;
}
</style>