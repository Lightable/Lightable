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
    $getSelfAvatar(): string | null {
        //@ts-ignore
        if (!this.user?.avatar) return null
        return `${this.api}/cdn/user/${this.user?.id}/avatars/avatar_${this.user?.avatar.id}`;
    }

    $getExternalAvatar(uid: string, avatarID: string) {
        return `${this.api}/cdn/user/${uid}/avatars/avatar_${avatarID}`;
    }
}


export interface LiteClientOptions {
    token: string;
    user: Account | null;
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
