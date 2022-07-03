import { defineStore } from "pinia";
import LiteClient from "../lib/LiteClient";
import { Account, Friend } from "../User";
import { useAppStore } from "./AppStore";


// @ts-ignore
export const useClientStore = defineStore('clientStore', {
    state: () => ({
            lite: new LiteClient({token: '', user: null}),
            friends: new Map<string, Friend>(),
            app: useAppStore()
    }),
    actions: {
        async login(email: string, pass: string) {
            let res = await fetch('https://api.zenspace.cf/user/@me/login', {
                method: 'POST',
                body: JSON.stringify({
                    email,
                    password: pass
                })
            })
            let json = await res.json(); 
            if (!res.ok) return json;
            this.app.account = json;
            this.lite = new LiteClient({token: json.token.token, user: json});
            return json
        }
    }
})