import { useAppStore } from "../stores/AppStore";
import { useClientStore } from "../stores/ClientStore";
import { Account, Friend } from "../User";
import EventEmitter from "./EventEmitter";




export declare interface LiteClient {
    on(event: 'self:avatar:upload:progress', listener: (p: number) => void): this;
    on(event: 'self:avatar:upload:finish', listener: (p: object) => void): this;
    on(event: 'self:avatar:upload:lewd', listener: (p: object) => void): this;
    on(event: 'self:avatar:upload:500', listener: (p: object) => void): this;
}
export class LiteClient extends EventEmitter {
    token: string;
    user: Account | null;
    api: string;
    store: any;
    app: any;
    constructor(options: LiteClientOptions) {
        super();
        this.token = options.token;
        this.user = options.user;
        this.api = 'http://localhost:8080';
        this.store = useClientStore();
        this.app = useAppStore();
    }
    async $getSelf() {
        let req = await this.$request<Account>('GET', '/user/@me', undefined);
        if (req.ok) {
            req.data!!.profileOptions = this.$convertStringBooleanKeyPair(req.data?.profileOptions!!);
            this.user = req.data;
            this.app.account = req.data;
        } else if (req.status == 403) {
            if (this.user) this.user.enabled = false
            this.app.account.enabled = false;
            return
        }
    }
    $convertStringBooleanKeyPair(map: Map<string, boolean>): Map<string, boolean> {
        let convertedValues = Object.entries(map);
        let realMap = new Map<string, boolean>();
        for (let pi = 0; convertedValues.length > pi; pi++) {
            // @ts-ignore
            let profileOptionPair = convertedValues[pi];
            let key = profileOptionPair[0];
            let value = profileOptionPair[1] as boolean;
            realMap.set(key, value);
        }
        return realMap
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
    async getInvitedUsers() {
        if (!this.user?.admin) return
        let req = await this.$request<Array<InviteRegister>>('GET', '/invite/list', undefined);
        if (req.status == 200) {
            this.store.invites = req.data;
        }
    }

    async acceptPendingInviteUser(email: string) {
        if (!this.user?.admin) return
        let req = await this.$request<InviteRegister>('POST', `/invite/accept?email=${email}`, undefined);
        if (req.status == 201) {
            let indexOfExisting = this.store.invites.findIndex((e: InviteRegister) => e.email == req?.data?.email) as number
            this.store.invites[indexOfExisting] = req.data;
            return email;
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
        return `${this.api}/cdn/user/${this.user?.id}/avatars/avatar_${this.user?.avatar.id}?size=128`;
    }

    $getExternalAvatar(uid: string, avatarID: string) {
        return `${this.api}/cdn/user/${uid}/avatars/avatar_${avatarID}?size=64`;
    }

    async $update(patch: UserPatch) {
        let req = await this.$request<Account>('PATCH', '/user/@me', JSON.stringify(patch));
        req.data!!.profileOptions = this.$convertStringBooleanKeyPair(req.data?.profileOptions!!);
        this.app.account = req.data;
        this.user = req.data;
        return req;
    }

    async $updateAvatar(file: File) {
        let form = new FormData();
        form.append('avatar', file, 'avatar');
        let request = new XMLHttpRequest();
        request.open('POST', `${this.api}/user/@me/avatar`);
        request.setRequestHeader('Authorization', this.token);
        request.upload.onprogress = (e: ProgressEvent) => {
            let { loaded, total } = e;
            let percentage = Math.ceil((loaded / total) * 100);
            this.emit('self:avatar:upload:progress', percentage);
        }
        request.send(form);
        request.onload = (d) => {
            let response = JSON.parse(request.responseText);
            switch (request.status) {
                case 200: {
                    this!!.user!!.avatar = response.avatar;
                    this.app.account.avatar = response.avatar;
                    return Promise.resolve(this.emit('self:avatar:upload:finish', response));
                }
                case 403: {
                    this.emit('self:avatar:upload:lewd', response);
                    break;
                }
                case 500: {
                    this.emit('self:avatar:upload:500')
                }
            }
        }
    }
    async getProfile(name: string) {

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
export interface UserPatch {
    name?: String,
    email?: String,
    profileOptions?: UserPatchProfilePair[]
}
export interface InviteRegister {
    email: string
    code?: string
}
export interface UserPatchProfilePair {
    key: string,
    value: boolean
}