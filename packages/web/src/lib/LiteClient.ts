import { useAppStore } from "../stores/AppStore";
import { useClientStore } from "../stores/ClientStore";
import { Account, Friend } from "../User";

export default class LiteClient {
    token: string;
    user: Account | null;
    api: string;
    store: any;
    app: any;
    constructor(options: LiteClientOptions) {
        this.token = options.token;
        this.user = options.user;
        this.api = 'http://localhost:8080';
        this.store = useClientStore();
        this.app = useAppStore();
    }
    async $getSelf() {
        let req = await this.$request<Account>('GET', '/user/@me', undefined);
        if (req.ok) {
            this.user = req.data;
            this.app.account = req.data;
        } else if (req.status == 403) {
            if (this.user) this.user.enabled = false
            this.app.account.enabled = false;
            return
        }
    }
    async $request<T>(method: string, path: string, body: string | undefined): Promise<ResponseData<T>> {
        let res = await fetch(`${this.api}${path}`, {
            method: method,
            body: body,
            headers: {
                'Authorization': this.token
            }
        })
        if (res.status != 204) {
            let json = await res.json();
            if (!res.ok) {
                return {
                    ok: false,
                    status: res.status,
                    error: json,
                    data: null
                }
            } else {
                let converted = json as T
                return {
                    ok: true,
                    status: res.status,
                    error: null,
                    data: converted
                }
            }
        } else {
            return {
                ok: true,
                status: 204,
                error: null,
                data: null
            }
        }
    }
    async getFriends() {
        if (!this.user?.enabled) return
        let req = await this.$request<RelationshipObject>('GET', '/user/@me/relationships', undefined);
        if (req.status != 204) {
            let friends = req.data!!.friends!!;
            for (let fi = 0; friends.length > fi; fi++) {
                let friend = friends[fi];
                console.log(`ID -> ${friend.id} NAME -> ${friend.name}`)
                this.store.friends.set(`${friend.id}`, friend);
            }
        }
    }

    async getEnabledUsers(type: UserSearchType | null, query: string | null) {
        if (!this.user?.admin) return
        let constructQuery = (type != null) ? `?type=${type}&search=${query}` : '';
        let req = await this.$request<UserPayload>('GET', `/admin/users/enabled${constructQuery}`, undefined);
        if (req.status != 204) {
            let users = req.data!!.users;
            for (let ui = 0; users.length > ui; ui++) {
                let user = users[ui];
                this.store.enabledUsers.set(`${user.id}`, user);
            }
        }
    }
    async getDisabledUsers(type: UserSearchType | null, query: string | null) {
        if (!this.user?.admin) return
        let constructQuery = (type != null) ? `?type=${type}&search=${query}` : '';
        let req = await this.$request<UserPayload>('GET', `/admin/users/disabled${constructQuery}`, undefined);
        if (req.status != 204) {
            let users = req.data!!.users;
            for (let ui = 0; users.length > ui; ui++) {
                let user = users[ui];
                this.store.disabledUsers.set(`${user.id}`, user);
            }
        }
    }

    async enableUser(id: string) {
        if (!this.user?.admin) return
        let req = await this.$request<UserPayload>('PATCH', `/admin/users/enable/${id}`, undefined);
        if (req.status == 204) {
            let user = this.store.disabledUsers.get(id);
            this.store.disabledUsers.delete(id);
            this.store.enabledUsers.set(id, user);
            return
        } else {
            console.log(`Something went wrong ${req}`);
        }
    }
    async disableUser(id: string) {
        if (!this.user?.admin) return
        let req = await this.$request<UserPayload>('PATCH', `/admin/users/disable/${id}`, undefined);
        if (req.status == 204) {
            let user = this.store.enabledUsers.get(id);
            this.store.enabledUsers.delete(id);
            this.store.disabledUsers.set(id, user);
            return
        } else {
            console.log(`Something went wrong ${req}`);
        }
    }
    $getSelfAvatar(): string | null {
        //@ts-ignore
        if (!this.user?.avatar) return null
        return `${this.api}/cdn/user/${this.user?.id}/avatars/avatar_${this.user?.avatar.id}`;
    }

    $getExternalAvatar(uid: string, avatarID: string) {
        return `${this.api}/cdn/user/${uid}/avatars/avatar_${avatarID}`;
    }
}

export type UserSearchType = "NAME" | "ID" | "CREATED"
export interface LiteClientOptions {
    token: string;
    user: Account | null;
}

export interface UserPayload {
    users: Friend[],
    size: number
}
export interface RelationshipObject {
    friends: Friend[],
    pending: Friend[],
    requests: Friend[]
}
export interface ResponseData<T> {
    ok: boolean;
    status: number;
    error: string | null;
    data: T | null;
}
