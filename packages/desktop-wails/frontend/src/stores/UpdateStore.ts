import { defineStore } from "pinia";
import { mocks } from  '../../wailsjs/go/models';

export const useUpdateStore = defineStore('updateStore', {
    state: () => ({
        currentUpdate: {
            available: false,
            data: null as mocks.Update | null,
            downloading: false,
            percentage: 0,
            finished: false,
        }
    }),
    actions: {
        setCurrentUpdate(update: mocks.Update) {
            this.currentUpdate.available = true;
            this.currentUpdate.data = update;
        }
    }
})


export interface IRelease {
    version: string,
    title: string,
    notes: string,
    signature: string,
    url: string
}

export class Release implements IRelease {
    version: string;
    title: string;
    notes: string;
    signature: string;
    url: string;
    constructor(data: IRelease) {
        this.version = data.version;
        this.title = data.title;
        this.notes = data.notes;
        this.signature = data.signature;
        this.url = data.url;
    }

}