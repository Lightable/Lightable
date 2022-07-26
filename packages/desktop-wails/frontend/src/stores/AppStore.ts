import { defineStore } from "pinia"
import { GetConfig, ChangeTheme, PingDelay } from '../../wailsjs/go/app/App'
import { GetSocketHistory } from '../../wailsjs/go/client/Client';
import { EventsOn } from '../../wailsjs/runtime/runtime';
import { debug } from '../composable/Logger'
export const useAppStore = defineStore('AppStore', {
    state: () => ({
        theme: 'Dark' as LightableTheme,

        drawers: {
            websocket: false
        },

        history: {
            websocket: [] as Array<string>
        }
    }),

    actions: {
        async load() {
            let config = await GetConfig()
            let history = await GetSocketHistory()
            this.theme = config.theme as LightableTheme;
            debug('Theme', `Current theme is "${config.theme}"`);
            if (history) {
                this.history.websocket = history;
                return
            }
            const start = performance.now()
            await PingDelay();
            debug('IPC', `IPC Delay is approx: ${(performance.now() - start).toFixed(3)}ms`);
        },
        async startRealtime() {
            EventsOn('ws:read:decode', async () => {
                this.history.websocket = await GetSocketHistory() as Array<string>
            });
        },
        async changeTheme(theme: LightableTheme) {
            this.theme = theme;
            await ChangeTheme(theme);
        }
    }
})


export type LightableTheme = "Dark" | "Light"


export interface AppConfig {
    theme: LightableTheme
}