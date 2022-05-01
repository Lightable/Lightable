import { ISelf, IUser } from "@/lib/structures/Users";
import { defineStore } from "pinia";
import { Store } from "tauri-plugin-store-api";
import { ClientStore } from "./ClientStore";
import { Nullable } from "@/lib/utils/null";
export const AuthenticationStore = defineStore('AuthenticationStore', {
    state: () => ({
        store: new Store('.accounts'),
        defaultStore: new Store('.default'),
        default: null as Nullable<ISelf>,
        accounts: new Map<string, ISelf>()
    }),
    actions: {
        setDefault(user: string) {
            this.default = this.accounts.get(user)!!;
            this.defaultStore.set('default', this.default);
            this.defaultStore.save();
        },
        addAccount(user: ISelf) {
            this.accounts.set(user.id, user);
            this.store.set(user.id, user);
            this.setDefault(user.id);
            this.store.save();
        },
        removeAccount(id: string | null, user: IUser) {
            let clientStore = ClientStore();
            if (id) {
                this.accounts.delete(id);
                this.store.delete(id);
                clientStore.accounts.delete(id);
            } else if (user) {
                this.accounts.delete(user.id);
                this.store.delete(user.id);
                clientStore.accounts.delete(user.id);
            }
            this.store.save();
        },
        async save() {
            await this.store.save();
        },
        async load() {
            let accounts = await this.store.values();
            this.default = await this.defaultStore.get('default') as Nullable<ISelf>;
            for (var account of accounts) {
                this.addAccount(account as unknown as ISelf);
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