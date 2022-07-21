<script setup lang="ts">
import { ref, defineAsyncComponent, computed } from 'vue'; 
import { useGameStore } from '../../../stores/GameStore';
import Connect4 from '../../../composables/games/Connect4';
import { useAppStore } from '../../../stores/AppStore';

const appStore = useAppStore();
const gameStore = useGameStore();
const id = gameStore.createConnect4(new Connect4(appStore!!.account!!.id,'urdad'));
const board = computed(() => gameStore.games.get(id));
const ZConnect4Row = defineAsyncComponent({
    loader: () => import('./ZConnect4Row.vue')
});

setInterval(() => {
    // @ts-ignore
    board.value.placeChecker(appStore!!.account!!.id, {x: 3, y: 0});
}, 5000)
</script>


<template>
    <div class="c4-board">
        <ZConnect4Row v-for="(item, index) in board?.board" :index="index" :row="item" :board="board"/>
    </div>
</template>


<style lang="scss" scoped>
.c4-board {
    display: table;
    border-collapse: separate;
    border-spacing: 5px;
}
</style>