import { User } from "@/lib/structures/Users";
import { defineStore } from "pinia";
import { Store } from 'tauri-plugin-store-api';
import { app } from '@tauri-apps/api'
import { Nullable } from "@/lib/utils/null";
import { Release } from "@/lib/structures/Release";
import { appWindow } from "@tauri-apps/api/window";
import { invoke } from '@tauri-apps/api';
import Logger from "@/lib/Logger";
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
        downloadingUpdateModalVis: false as boolean,
        currentlyActiveProfile: null as string | null | User,
        currentPanelAct: "None" as CurrentPanelAct,
        debug: false as boolean,
        notFoundModal: false as boolean,
        offline: false as boolean,
        version: 'unknown',
        experiments: new Map<string, boolean>(),
        showExperiments: false as boolean,
        update: null as Nullable<Release>,
        navigatedToNewChannel: false,
        accent: '' as string,
        $logger: new Logger()
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
        setDownloadingModalVis(bool: boolean) {
            this.downloadingUpdateModalVis = bool;
        },
        setExperiment(key: string, value: boolean) {
            switch (key) {
                case 'micaChat': {
                    if (value) {
                        invoke('set_mica', { mica: true })
                    } else {
                        invoke('set_mica', { mica: false })
                    }
                    break;
                }
                case 'blurChat': {
                    if (value) {
                        invoke('set_blur', { blur: true })
                    } else {
                        invoke('set_blur', { blur: false })
                    }
                    break;
                }
                case 'acrylicChat': {
                    if (value) {
                        invoke('set_acrylic', { acrylic: true })
                    } else {
                        invoke('set_acrylic', { acrylic: false })
                    }
                    break;
                }
            }
            this.experiments.set(key, value);
            this.store.set('experiments', Object.fromEntries(this.experiments));
            this.save();
        },
        enableExperiments(bool: boolean) {
            this.showExperiments = bool;
            this.store.set('showExperiments', bool);
            this.save();
        },
        setNavigatedToNewChannel(bool: boolean) {
            this.navigatedToNewChannel = bool;
        },
        async save() {
            await this.store.save();
        },
        async load() {
            await this.store.load();
            await this.$loadExperiments();
            let debug = await this.store.get('debug') as boolean;
            let theme = await invoke('get_accent_color');
            this.version = await app.getVersion();

            this.accent = `#${theme}`;
            console.log(`Got Sys Theme %c(${this.accent})`,);
            this.setDebug(debug);
        },
        async $loadExperiments() {
            this.enableExperiments(await this.store.get('showExperiments') as boolean);
            let experimentEntries = Object.entries(await this.store.get('experiments') as Object);
            if (experimentEntries.length < 1) {
                this.experiments.set('profileNotice', false);
                this.experiments.set('messages', false);
                this.experiments.set('dmSettings', false);
                this.experiments.set('micaChat', false);
                this.experiments.set('acrylicChat', false);
                this.experiments.set('blurChat', false);
                return
            }
            for (let ee = 0; experimentEntries.length > ee; ee++) {
                let experimentKeyPair = experimentEntries[ee];
                let key = experimentKeyPair[0];
                let value = experimentKeyPair[1];
                this.experiments.set(key, value);
                this.$logger.log(`Zenspace -> App Load -> Load Experiments`, `Loaded Experiment "${key}"`, value);
            }
        }
    }
})

export type CurrentPanelAct = "Friends" | "Groups" | "Settings" | "Admin" | "Experiments" | "None"