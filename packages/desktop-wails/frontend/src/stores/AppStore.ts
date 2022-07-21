import { defineStore } from "pinia"
import { GetConfig, ChangeTheme } from '../../wailsjs/go/main/App'

export const useAppStore = defineStore('AppStore', {
    state: () => ({
        theme: 'Dark' as LightableTheme,
    }),

    actions: {
        async load() {
            let config = await GetConfig() as AppConfig
            this.theme = config.Theme;
        },

        async changeTheme(theme: LightableTheme) {
            this.theme = theme;
            await ChangeTheme(theme);
        }
    }
})


export type LightableTheme = "Dark" | "Light"


export interface AppConfig {
    Theme: LightableTheme
}