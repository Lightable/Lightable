<script setup lang="ts">
import { PropType } from 'vue';
import confetti, { Options } from 'canvas-confetti';
import { useConfettiStore } from '../../stores/ConfettiStore';
const emit = defineEmits(['destroy']);
const props = defineProps({
    id: String,
    options: Object as PropType<Options>,
});
let confettiStore = useConfettiStore();

console.log('Confetti instance created with options ', props.options)


</script>

<template>
    <canvas :id="id" :style="{ height: '100%', width: '100%', position: 'absolute', overflow: 'hidden', zIndex: 2000, pointerEvents: 'none' }" />
</template>

<script lang="ts">
export default {
    mounted() {
        // @ts-ignore
        const instance = confetti.create(document.getElementById(this.props.id!!) as HTMLCanvasElement, {
            useWorker: true,
            resize: true
        })
        // @ts-ignore
        instance({ ...this.props.options })?.then(() => this.confettiStore.confetti.delete(this.props.id!!));
    }
}
</script>