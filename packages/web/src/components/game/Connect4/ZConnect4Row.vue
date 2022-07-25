<script setup lang="ts">
import { defineAsyncComponent, PropType, ref } from 'vue';
import Connect4, { Checker } from '../../../composables/games/Connect4';
const props = defineProps({
    row: Array as PropType<Array<Checker | null>>,
    index: Number as PropType<number>,
    board: Object as PropType<Connect4>
});
const ZC4Item = defineAsyncComponent({
    loader: () => import('./ZConnect4Item.vue')
});
</script>


<template>
    <tr class="c4-row">
        <ZC4Item class="c4-item" v-for="(item, itemPos) in row" v-bind:key="index" :pos="{x: itemPos, y: index!!}" :item="(item) ? item : undefined" :board="board"/>
    </tr>
</template>

<style lang="scss" scoped>
.c4-row {
    padding: 8px;
    
}
</style>