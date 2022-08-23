<script setup lang="ts">
import { onMounted, PropType } from 'vue';
import confetti, { Options } from 'canvas-confetti';
import { useConfettiStore } from '../../stores/ConfettiStore';
const emit = defineEmits(['destroy']);
const props = defineProps({
    id: String,
    options: Object as PropType<Options>,
});
const confettiStore = useConfettiStore()
onMounted(() => {
    (`[ConfettiFactory -> Create]: ${props.id}`);
    const instance = confetti.create(document.getElementById(props.id!!) as HTMLCanvasElement, {
        useWorker: false,
        resize: true
    })
    instance({ ...props.options })?.then(() => confettiStore.confetti.delete(props.id!!));
})
</script>

<template>
    <canvas :id="id" :style="{ height: '100%', width: '100%', position: 'absolute', overflow: 'hidden', zIndex: 2000, pointerEvents: 'none' }" />
</template>
