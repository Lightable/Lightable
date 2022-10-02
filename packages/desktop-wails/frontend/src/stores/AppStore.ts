import {defineStore} from "pinia";
import {ChangeTheme, GetColour, GetConfig, GetVersion, PingDelay} from '../../wailsjs/go/app/App';
import {GetSocketHistory, GetUpdate} from '../../wailsjs/go/client/Client';
import {mocks} from "../../wailsjs/go/models";
import {EventsOn, WindowSetTitle, WindowSetBackgroundColour} from '../../wailsjs/runtime';
import {debug} from '../composable/Logger';
import {GetRelations} from '../../wailsjs/go/client/RelationshipManager';
import {useUpdateStore} from "./UpdateStore";

export const useAppStore = defineStore('AppStore', {
    state: () => ({
        events: new EventEmitter(),
        version: 'Unknown Version' as string,
        shouldShowOnboardModal: false,
        theme: 'Dark' as LightableTheme,
        colour: '',
        hasUser: false as boolean,
        user: null as mocks.PrivateUser | null,
        avatar: null as string | null,
        drawers: {
            websocket: true
        },
        settings: {
            currentDrawer: '',
        },
        leftDrawer: {
            show: false,
            collapsed: false,
            components: [] as Array<LightableDrawerComponentPair>,
            groups: [] as Array<LightableDrawerGroupComponent>,
        },
        history: {
            websocket: [] as Array<string>
        },
        overrideExistingUser: false,
        users: [] as Array<mocks.PublicUser>,
        relationships: new mocks.RelationshipStruct(),
        search: {
            show: false
        },
        customTheme: CustomTheme
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
            if (!config.responder) this.shouldShowOnboardModal = true;
            let root = document.documentElement;
            this.colour = colour;
            root.style.setProperty("--windows-accent-colour", this.colour);
            this.loadCustomStyling(this.theme)
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
            const updateStore = useUpdateStore();
            EventsOn('ws:read:decode', async () => {
                this.history.websocket = await GetSocketHistory() as Array<string>
            });
            EventsOn('ws:read:server|start', async (d) => {
                this.relationships = await GetRelations();
                this.user = d.user;
            });
            EventsOn('ws:read:user|status', async (_) => {
                this.relationships = await GetRelations();
                this.events.emit('user|update', _);
            });
            EventsOn('ws:read:server|pending', async (_) => {
                this.relationships = await GetRelations();
                this.updateDrawerComponent('friends', {
                    badge: {
                        show: true,
                        type: 'error',
                        closableOnClick: true,
                    }
                })
            })
            EventsOn('ws:read:server|update', async (_) => {
                updateStore.currentUpdate.data = await GetUpdate();
                updateStore.currentUpdate.available = true;
                this.updateDrawerComponent('settings', {
                    badge: {
                        show: true,
                        type: 'success',
                        processing: true,
                        closableOnClick: false,
                    },
                    tooltip: 'Update Available'
                })
            })
        },
        async changeTheme(theme: LightableTheme) {
            this.theme = theme;
            ChangeTheme(theme);
            this.loadCustomStyling(theme)
        },
        getUserTypeRelation(us?: string): RelationshipStatus | undefined {
            if (!us) return
            let f = this.relationships.friends.find(u => u.id === us)
            let p = this.relationships.pending.find(u => u.id === us)
            let r = this.relationships.requests.find(u => u.id === us)
            if (f) {
                return RelationshipStatus.FRIEND
            } else if (p) {
                return RelationshipStatus.PENDING
            } else if (r) {
                return RelationshipStatus.REQUEST
            }
            return RelationshipStatus.UNKNOWN
        },
        setTitle(name: string) {
            WindowSetTitle(`Lightable｜${name}`);
        },
        updateDrawerComponent(name: string, component: Partial<LightableDrawerComponentPair>) {
            let comp = this.leftDrawer.components[this.leftDrawer.components.findIndex(f => f.name == name)];
            if (comp) {
                if (component.t) comp.t = component.t;
                if (component.icon) comp.icon = component.icon;
                if (component.text) comp.text = component.text;
                if (component.cb) comp.cb = component.cb;
                if (component.path) comp.path = component.path;
                if (component.name) comp.name = component.name;
                if (component.color) comp.color = component.color;
                if (component.tooltip) comp.tooltip = component.tooltip;
                if (component.badge) comp.badge = component.badge;
            }
        },
        loadCustomStyling(theme: LightableTheme) {
            let root = document.documentElement;
            const setProperty = (prop: string, value: any) => {
                root.style.setProperty(prop, value)
            }
            switch (theme) {
                case "Dark": {
                    setProperty("--lightable-header-color", "var(--lightable-dark-header-color)");
                    setProperty("--lightable-drawer-color", "var(--lightable-dark-drawer-color)");
                    setProperty("--lightable-card-color", "var(--lightable-dark-card-color)");
                    WindowSetBackgroundColour(16, 16, 20, 1);
                    break
                }
                case "Light": {
                    setProperty("--lightable-header-color", "var(--lightable-light-header-color)");
                    setProperty("--lightable-drawer-color", "var(--lightable-light-drawer-color)");
                    setProperty("--lightable-card-color", "var(--lightable-light-card-color)");
                    WindowSetBackgroundColour( 	255, 255, 255, 1);
                    break
                }
            }
        }
    }
})


export type LightableTheme = "Dark" | "Light"
export enum RelationshipStatus {
    UNKNOWN = 1,
    REQUEST = 2,
    PENDING = 3,
    FRIEND = 4
}

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
    path?: string,
    name?: string,
    color?: string | '#fff',
    tooltip?: string,
    badge?: {
        show: boolean,
        closableOnClick?: boolean,
        type?: 'default' | 'success' | 'error' | 'warning' | 'info',
        max?: number,
        color?: string,
        processing?: boolean,
        icon?: any,
        value?: string | number,
    }
}

let CustomTheme =  {
    "primaryColor": "#63e2b7",
    "primaryColorHover": "#7fe7c4",
    "primaryColorPressed": "#5acea7",
    "primaryColorSuppl": "rgb(42, 148, 125)",
    "infoColor": "#70c0e8",
    "infoColorHover": "#8acbec",
    "infoColorPressed": "#66afd3",
    "infoColorSuppl": "rgb(56, 137, 197)",
    "successColor": "#63e2b7",
    "successColorHover": "#7fe7c4",
    "successColorPressed": "#5acea7",
    "successColorSuppl": "rgb(42, 148, 125)",
    "warningColor": "#f2c97d",
    "warningColorHover": "#f5d599",
    "warningColorPressed": "#e6c260",
    "warningColorSuppl": "rgb(240, 138, 0)",
    "errorColor": "#e88080",
    "errorColorHover": "#e98b8b",
    "errorColorPressed": "#e57272",
    "errorColorSuppl": "rgb(208, 58, 82)",
    "textColorBase": "#fff",
    "textColor1": "rgba(255, 255, 255, 0.9)",
    "textColor2": "rgba(255, 255, 255, 0.82)",
    "textColor3": "rgba(255, 255, 255, 0.52)",
    "textColorDisabled": "rgba(255, 255, 255, 0.38)",
    "placeholderColor": "rgba(255, 255, 255, 0.38)",
    "placeholderColorDisabled": "rgba(255, 255, 255, 0.28)",
    "iconColor": "rgba(255, 255, 255, 0.38)",
    "iconColorDisabled": "rgba(255, 255, 255, 0.28)",
    "iconColorHover": "rgba(255, 255, 255, 0.475)",
    "iconColorPressed": "rgba(255, 255, 255, 0.30400000000000005)",
}
export type LightableDrawerType = "Function" | "Route"



class EventEmitter{
    constructor(){
        // @ts-ignore
        this.callbacks = {}
    }
    // @ts-ignore
    on(event, cb){
        // @ts-ignore
        if(!this.callbacks[event]) this.callbacks[event] = [];
        // @ts-ignore
        this.callbacks[event].push(cb)
    }
    // @ts-ignore
    emit(event, data){
        // @ts-ignore
        let cbs = this.callbacks[event]
        if(cbs){
            // @ts-ignore
            cbs.forEach(cb => cb(data))
        }
    }
}