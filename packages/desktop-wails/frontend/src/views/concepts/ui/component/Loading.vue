<script setup lang="ts">
import Loading from '../../../../components/loading/Loading.vue';
import { ref } from 'vue';
import { NButton, NButtonGroup } from 'naive-ui';
import { LoadingStates } from '../../../../composable/LoadingData';
const loadingSteps = ref([
    {
        finished: false,
        name: 'Login',
        state: LoadingStates.SUCCESS
    },
    {
        finished: false,
        name: 'Connect to WebSocket',
        state: LoadingStates.WARNING
    },
    {
        finished: false,
        name: 'Get messages',
        state: LoadingStates.FAILED
    },
    {
        finished: false,
        name: 'Finishing up',
        state: LoadingStates.SUCCESS
    },
    
]);

let current = ref(0);

const moveTowards = (back: boolean) => {
    if (back) {
        if (current.value <= 0) return
        current.value -= 1;
        loadingSteps.value[current.value].finished = false

    } else {
        if (current.value > loadingSteps.value.length - 1) return
        current.value += 1;
        loadingSteps.value[current.value - 1].finished = true

    }
} 
</script>

<template>
    {{ loadingSteps }}
    <div class="controls">
        <NButtonGroup>
            <NButton round tertiary @click="moveTowards(true)">⬅️</NButton>
            <NButton round tertiary @click="moveTowards(false)">➡️</NButton>
        </NButtonGroup>
    </div>
    <div class="preview load">
        <Loading :steps="loadingSteps" />
    </div>
</template>


<style lang="scss" scoped>
.preview {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: inherit;
    width: 100%;
    gap: 16px;
}
</style>