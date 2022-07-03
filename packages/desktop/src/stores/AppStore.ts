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
            console.log(`Setting experiment "${key}" to`, value);
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
                        setTimeout(() => {
                            invoke('set_acrylic', { acrylic: true, r: 0, g: 0, b: 0, opacity: 0 });
                        }, 200)

                    } else {
                        invoke('set_acrylic', { acrylic: false, r: 0, g: 0, b: 0, opacity: 0 })
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
            this.setExperiment('profileNotice', false);
            this.setExperiment('messages', false);
            this.setExperiment('dmSettings', false);
            this.setExperiment('micaChat', false);
            this.setExperiment('acrylicChat', false);
            this.setExperiment('blurChat', false);
            await this.$loadExperiments();
            let debug = await this.store.get('debug') as boolean;
            let theme = await invoke('get_accent_color');
            this.version = await app.getVersion();
            this.accent = `#${theme.toString(16).substring(2)}`;
            var r = document.querySelector(':root') as any;
            r.style.setProperty('--primaryAccent', this.accent);
            console.log(`Got Sys Theme %c(${this.accent})`, `color: ${this.accent};`);
            this.setDebug(debug);
            // Debug Purposes
            this.setRelease({
                version: '0.0.1',
                title: 'Fake Release',
                notes: 'Fake release notes',
                signature: 'NotRealReleaseSig',
                url: 'https://google.com'
            });
        },
        async $loadExperiments() {
            this.enableExperiments(await this.store.get('showExperiments') as boolean);
            let experimentEntries: [string, any];
            let savedRawExperimentEntries = await this.store.get('experiments');
            if (savedRawExperimentEntries == null || savedRawExperimentEntries == undefined) {
                experimentEntries = {} as [string, any];
            } else {
                experimentEntries = Object.entries(savedRawExperimentEntries as Object) as [string, any];
            }
            console.log(experimentEntries.length);
            for (let ee = 0; experimentEntries.length > ee; ee++) {
                let experimentKeyPair = experimentEntries[ee];
                let key = experimentKeyPair[0];
                let value = experimentKeyPair[1];
                this.setExperiment(key, value);
                this.$logger.log(`Zenspace -> App Load -> Load Experiments`, `Loaded Experiment "${key}"`, value);
            }
        }
    }
})

export type CurrentPanelAct = "Friends" | "Groups" | "Settings" | "Admin" | "Experiments" | "None"