import { defineStore } from "pinia";

export const useUpdateStore = defineStore('updateStore', {
    state: () => ({
        currentUpdate: {
            avaliable: false,
            data: null as Release | null
        }
    }),
    actions: {
        setCurrentUpdate(update: IRelease) {
            this.currentUpdate.avaliable = true;
            this.currentUpdate.data = new Release(update);
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