import { defineStore } from "pinia"
import { Account} from "../User";


export const useAppStore = defineStore('appStore', {
    state: () => ({
            isDark: true,
            account: null as Account | null,
    }),
    persist: {
        key: 'app',
        storage: window.localStorage,
        paths: ['isDark','account'],
        beforeRestore: context => {
            console.log('Restoring session...');
        },
        afterRestore: context => {
            console.log('Restored session!');
        }
    },

    actions: {
        setTheme(isDark: boolean) {
            this.isDark = isDark;
            document.getElementById('app')!!.style.backgroundColor = (this.isDark) ? 'var(--bg-dark)' : 'var(--bg-light)';
        },
    }
})