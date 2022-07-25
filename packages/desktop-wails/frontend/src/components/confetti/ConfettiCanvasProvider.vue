<script setup lang="ts">
import { computed } from 'vue';
import { useConfettiStore } from '../../stores/ConfettiStore';
import CanvasConfetti from './CanvasConfetti.vue';
let confettiStore = useConfettiStore();
const confettiList = computed(() => Array.from(confettiStore.confetti.values()))
</script>

<template>
    <div class="z-confetti-provider">
        <CanvasConfetti v-for="(confettiInst, _) in confettiList" :options="confettiInst.options" :id="`z-confetti-${confettiInst.key}`" v-bind:key="confettiInst.key"/>
        <div class="z-confetti-container">
            <slot/>
        </div>

    </div>
</template>

