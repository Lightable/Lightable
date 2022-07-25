import { defineStore } from "pinia";
import { InviteRegister, LiteClient } from "../lib/LiteClient";
import { Account, Friend } from "../User";
import { useAppStore } from "./AppStore";

export const useClientStore = defineStore('clientStore', {
    state: () => ({
            lite: new LiteClient({token: '', user: null}),
            friends: new Map<string, Friend>(),
            enabledUsers: new Map<string, Friend>(),
            disabledUsers: new Map<string, Friend>(),
            invites: new Array<InviteRegister>() 
    }),
    actions: {
        async login(email: string, pass: string) {
            let app = useAppStore();
            let res = await fetch('https://api.zenspace.cf/user/@me/login', {
                method: 'POST',
                body: JSON.stringify({
                    email,
                    password: pass
                })
            })
            let json = await res.json(); 
            if (!res.ok) return json;
            app.account = json;
            this.lite = new LiteClient({token: json.token.token, user: json});
            return json
        }
    }
})