import { User } from "@/lib/structures/Users";
import { defineStore } from "pinia";
import { Store } from 'tauri-plugin-store-api';
import { app } from '@tauri-apps/api'
import { Nullable } from "@/lib/utils/null";
import { Release } from "@/lib/structures/Release";
export const AppStore = defineStore('AppStore', {
    state: () => ({
        store: new Store('.app'),
        notifications: new Array(),
        device: {
            power: 'Unknown'
        },
        titlebar: true,
        titleDetails: null as string | null,
        addFriendModalVis: false as boolean,
        newNameModalVis: false as boolean,
        newReleaseModalVis: false as boolean,
        currentlyActiveProfile: null as string | null | User,
        currentPanelAct: "None" as CurrentPanelAct,
        debug: false as boolean,
        notFoundModal: false as boolean,
        offline: false as boolean,
        version: 'unknown',

        update: null as Nullable<Release>
    }),
    actions: {
        showNotFoundModal(show: boolean) {
            this.notFoundModal = show; 
        },
        allowTitlebar(bool: boolean) {
            this.titlebar = bool;
        },
        setTitleDetails(local: string | null) {
            this.titleDetails = local;
        },
        setCurrentPanel(panel: CurrentPanelAct) {
            this.setTitleDetails(`${panel}`);
            this.currentPanelAct = panel;
        },
        setFriendModalVis(bool: boolean) {
            this.addFriendModalVis = bool;
        },
        setNewNameModalVis(bool: boolean) {
            this.newNameModalVis = bool;
        },
        setNewReleaseModalVis(bool: boolean) {
            this.newReleaseModalVis = bool;
        },
        setDebug(bool: boolean) {
            this.debug = bool;
            this.store.set('debug', bool);
            this.save();
        },
        setCurrentlyActiveProfile(d: string | User | null) {
            this.currentlyActiveProfile = d;
        },
        setOffline(bool: boolean) {
            this.offline = bool;
        },
        setRelease(release: Release) {
            this.update = release;
        },
        async save() {
            await this.store.save();
        },
        async load() {
            await this.store.load();
            let debug = await this.store.get('debug') as boolean;
            this.version = await app.getVersion();
            this.setDebug(debug);
        }
    }
})

export type CurrentPanelAct = "Friends" | "Groups" | "Settings" | "Admin" | "None"