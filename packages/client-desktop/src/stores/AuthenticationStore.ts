import { ISelf } from "@/lib/structures/Users";
import { defineStore } from "pinia";
import { Store } from "tauri-plugin-store-api";

export const AuthenticationStore = defineStore('AuthenticationStore', {
    state: () => ({
        store: new Store('.accounts'),
        accounts: new Map<string, AuthenticationPartialUser>()
    }),
    actions: {
        addAccount(user: ISelf | AuthenticationPartialUser) {
            let authUser = new AuthenticationPartialUser(user.name, user.id, user.auth)
            this.accounts.set(user.id, authUser);
            this.store.set(user.id, authUser);
        },
        removeAccount(id: string | null, user: ISelf) {
            if (id) {
                this.accounts.delete(id);
                this.store.delete(id);
            } else if (user) {
                this.accounts.delete(user.id);
                this.store.delete(user.id);
            }
        },
        async save() {
            await this.store.save();
        },
        async load() {
            let accounts = await this.store.entries();
            for (var account of accounts) {
                this.addAccount(<AuthenticationPartialUser>account[1]);
            }
        }
    }
})

export interface IPartialUser {
    name: string,
    id: string
}
export interface IPartialSelf {
    authentication: string | null
}
export class PartialSelfUser implements IPartialUser, IPartialSelf {
    name: string;
    id: string;
    authentication: string;
    constructor(name: string, id: string, authentication: string) {
        this.name = name;
        this.id = id;
        this.authentication = authentication;
    }
}
export class AuthenticationPartialUser extends PartialSelfUser {
    added: number;
    constructor(name: string, id: string, authentication: string) {
        super(name, id, authentication);
        this.added = Date.now();
    }
}