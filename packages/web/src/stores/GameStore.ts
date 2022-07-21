import { defineStore } from "pinia";
import Connect4 from "../composables/games/Connect4";
import { createId } from 'seemly';


export const useGameStore = defineStore('gameStore', {
    state: () => ({
        games: new Map<string, Connect4>()
    }),

    actions: {
        createConnect4(c4: Connect4) {
            const id = createId()
            this.games.set(id, c4);
            return id;
        }
    }
})