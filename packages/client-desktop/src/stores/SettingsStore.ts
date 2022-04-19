import { defineStore } from "pinia";
import Logger from "@/lib/Logger";
import { Store } from 'tauri-plugin-store-api';
export const SettingsStore = defineStore('SettingsStore', {
    state: () => ({
        store: new Store('.settings'),
        element: document.documentElement,
        themes: {
            default: "dark" as DefaultThemes,
            current: 'dark'
        },
        notifications: {
            allowed: true,
            blocked: new Array<string>()
        } as Notifications,
        logger: new Logger()
    }),
    actions: {
        /* Themes */
        getTheme: (name: DefaultThemes) => getDefaultTheme(name),
        setCurrentTheme(theme: DefaultThemes) {
            if (theme == null) return
            this.store.set('currentTheme', theme);
            this.themes.current = theme;
            this.element.setAttribute("theme", theme);
            this.logger.logInfo('SettingStore', `SetCurrentTheme`, theme);
            this.save();
        },
        /* Notifications */
        allowNotifications(bool: boolean) {
            this.notifications.allowed = bool;
            this.store.set('notifications', this.notifications);
            this.logger.logInfo('SettingStore', `AllowNotifications`, bool);
        },
        addBlockUser(user: string) {
            this.notifications.blocked.push(user);
            this.store.set('notifications', this.notifications);
        },
        removeBlockedUser(user: string) {
            let index = this.notifications.blocked.indexOf(user)
            if (index == -1) throw new SyntaxError(`User doesn't exist`);
            this.notifications.blocked = this.notifications.blocked.filter(obj => { return obj !== user });
            this.store.set('notifications', this.notifications);
            return user;
        },
        /* Main */
        async save() {
            await this.store.save();
        },
        async load() {
            await this.store.load();
            let currentTheme = await this.store.get('currentTheme') as DefaultThemes
            let notifications = await this.store.get('notifications') as Notifications;
            this.setCurrentTheme(currentTheme);
            this.notifications = notifications;
            console.log(this.store.entries());
        }
    }
});


export interface Notifications {
    allowed: boolean;
    blocked: Array<string>
}

export class Theme {
    style: string | null;
    creator: string;
    name: string;
    constructor(style: string | null, creator: string, name: string) {
        this.style = style;
        this.creator = creator;
        this.name = name;
    }

}
export type DefaultThemes = "dark" | "ocean" | "fruit" | "liberty" | "moon" | "new";
const getDefaultTheme = async (name: DefaultThemes) => {
    let style = (await import(`../assets/sass/themes/${name}.scss`) as any).default;
    return new Theme(style, getCreator(style), name);
}
const getCreator = (styles: string) => {
    let regexp = /(--creator: )+("(?:\\==.|[^"\\])*")/gi;
    return getFirstGroup(regexp, styles);
}
function getFirstGroup(regexp: RegExp, str: string) {
    const array = [...str.matchAll(regexp)];
    return array.map(m => m[2])[0].replace(/['"]+/g, "");
}
