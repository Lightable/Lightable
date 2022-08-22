import { defineStore } from "pinia";

export const useSettingsStore = defineStore('settingsStore', {
    state: () => ({
        openPane: 'None' as SettingsPane
    }),
    actions: {
        setLivePane(pane: SettingsPane) {
            this.openPane = pane;
        }
    }
})


export type SettingsPane = "None" | "Debug" | "Debug Socket" | "User" | "Customization" 