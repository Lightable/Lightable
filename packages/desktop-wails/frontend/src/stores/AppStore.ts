import {defineStore} from "pinia";
import {ChangeTheme, GetColour, GetConfig, GetVersion, PingDelay} from '../../wailsjs/go/app/App';
import {GetSocketHistory, GetUpdate} from '../../wailsjs/go/client/Client';
import {mocks} from "../../wailsjs/go/models";
import {EventsOn, WindowSetTitle} from '../../wailsjs/runtime/runtime';
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
        }
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
            await ChangeTheme(theme);
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