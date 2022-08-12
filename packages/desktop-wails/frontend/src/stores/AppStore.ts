import { defineStore } from "pinia";
import { GetConfig, ChangeTheme, PingDelay, GetVersion, GetColour } from '../../wailsjs/go/app/App'
import { GetSocketHistory } from '../../wailsjs/go/client/Client';
import { mocks } from "../../wailsjs/go/models";
import { EventsOn } from '../../wailsjs/runtime/runtime';
import { debug } from '../composable/Logger';
import { GetRelations } from '../../wailsjs/go/client/RelationshipManager';
export const useAppStore = defineStore('AppStore', {
    state: () => ({
        version: 'Unknown Version' as string,
        theme: 'Dark' as LightableTheme,
        colour: '',
        hasUser: false as boolean,

        drawers: {
            websocket: false
        },
        leftDrawer: {
            show: false,
            components: [] as Array<LightableDrawerComponentPair>,
            groups: [] as Array<LightableDrawerGroupComponent>,
        },
        history: {
            websocket: [] as Array<string>
        },

        relationships: new mocks.RelationshipStruct()
    }),

    actions: {
        async load() {
            let config = await GetConfig();
            let history = await GetSocketHistory();
            let version = await GetVersion();
            let colour = await GetColour();
            this.theme = config.theme as LightableTheme;
            this.hasUser = config.hasUser;
            this.version = version;
            let root = document.documentElement;
            this.colour = colour;
            root.style.setProperty("--windows-accent-colour", this.colour);
            debug('Theme', `Current theme is "${config.theme} With accent colour "${this.colour}" `);
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
            EventsOn('ws:read:server|start', async () => {
                this.relationships = await GetRelations();
            });
            EventsOn('ws:read:user|status', async (_) => {
                this.relationships = await GetRelations();
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
export interface LightableDrawerGroupComponent {
    name: string,
    items: LightableDrawerComponentPair[]
}
export interface LightableDrawerComponentPair {
    t: LightableDrawerType,
    icon?: any,
    text: string,
    cb?: Function,
    path?: string
    color?: string | '#fff',
}

export type LightableDrawerType = "Function" | "Route"