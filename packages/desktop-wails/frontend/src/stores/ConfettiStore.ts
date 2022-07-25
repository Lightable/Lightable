import { defineStore } from "pinia";
import { Options } from "canvas-confetti";
import { createId } from 'seemly';
export const useConfettiStore = defineStore('confettiStore', {
    state: () => ({
        confetti: new Map<string, Confetti>()
    }),
    actions: {
        create(options: Options): Confetti {
            const key = createId();
            const confettiInstance = {
                key,
                options,
                destroy: () => {
                    this.confetti.delete(key);
                }
            } as Confetti
            this.confetti.set(`z-confetti-${key}`, confettiInstance);
            return confettiInstance;
        },
        destroyAll() {
            this.confetti.clear();
        },
        destroy(id: string) {
            this.confetti.delete(id);
        }
    }
})


export interface Confetti {
    key: string;
    options: Options
    destroy(): void;
}