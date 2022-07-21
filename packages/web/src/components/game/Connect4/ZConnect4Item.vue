<script setup lang="ts">
import { PropType } from 'vue';
import Connect4, { Checker, IPlayerPos } from '../../../composables/games/Connect4';

const props = defineProps({
    pos: Object as PropType<IPlayerPos>,
    item: Object as PropType<Checker>,
    board: Object as PropType<Connect4>
});

const place = () => {
  props.board?.placeChecker('urdad', props.pos!! as IPlayerPos);
}
</script>


<template>
    <td class="c4-item" :pos="JSON.stringify(pos)" :player="(item == undefined) ? 'none' : item.player == board?.players[0]" @click="place"/>
</template>


<style lang="scss" scoped>
.c4-item {
    background: rgb(48, 48, 48);
    border-radius: 50%;
    height: 24px;
    width: 24px;
    padding: 8px;
    margin: 4px;
  &[player='true'] {
    background-color: rgb(86, 181, 245);
  }
  &[player='false'] {
    background-color: rgb(245, 86, 86);
  }
}
</style>