import Logger from "@/lib/Logger";
import EventEmitter from "events";
import { defineStore } from "pinia";
// @ts-ignore 
import { v4 as uuidv4 } from 'uuid';
export const SnackStore = defineStore('SnackStore', {
    state: () => ({
        snacks: new Map<string, Snack>(),
        logger: new Logger()
    }),
    actions: {
        /**
         * Create a snack to show up in the Snackbar
         * @param type Snacktype
         * @param text Snack text
         * @param close Snack closeable
         * @param timeout Snack timeout till close
         * @returns Snack ID
         */
        create(type: SnackType, text: string, close: boolean, timeout: number | undefined) {
            let snack = new Snack(type, text, close, timeout);
            this.snacks.set(snack.id, snack);
            this.logger.logSuccess('SnackStore', `CreateSnack ${snack.id}`, snack);
            return snack;
        },
        remove(id: string) {
            this.snacks.delete(id);
            this.logger.logWarn('SnackStore', `DeleteSnack ${id}`);
        },
        update(id: string, newSnack: Snack) {
            let existing = this.snacks.get(id)
            existing = newSnack;
            this.snacks.set(existing.id, newSnack);
            this.logger.logInfo('SnackStore', `UpdateSnack ${id}`, newSnack);
        }
    }
})

export type SnackType = 'info' | 'error' | 'warning' | 'success' | 'loading'
export class Snack extends EventEmitter {
    /**
     * Snack construct
     * @param {SnackType} type Different type of snacks you can have
     * @param {String} text Text for snack
     * @param {Boolean} close Snack is closeable
     * @param {Number} timeout Timeout for snack to auto close
     */
    id: string;
    type: SnackType;
    text: string;
    close: boolean;
    timeout: number | undefined;
    constructor(type: SnackType, text: string, close: boolean, timeout: number | undefined) {
        super();
        this.id = uuidv4();
        this.type = type;
        this.text = text;
        this.close = close;
        this.timeout = timeout;
    }
}
