import { PartialSelfUser } from "@/stores/AuthenticationStore";
import { Client } from "../Client";
import { Nullable } from "../utils/null";
import { Attachment } from "./Attachment";
import Messages from "./Messages";

export enum RelationshipStatus {
    Unknown = "Unknown",
    None = "None",
    User = "User",
    Friend = "Friend",
    Outgoing = "Outgoing",
    Incoming = "Incoming",
    Blocked = "Blocked",
}

export enum DetailedRelationship {
    Taken = "Taken",
    Single = "Single",
    Other = "Other",
    Private = "Private"
}

export enum OnlineStatus {
    Offline = 0,
    DND = 1,
    ONLINE = 2
}
export interface IRelationship {
    state: RelationshipStatus,
    detailed: DetailedRelationship
}
export interface IStatus {
    text: Nullable<string>;
    icon: Nullable<Attachment>;
}
export interface IUser {
    badges: Nullable<number>;
    id: string;
    name: string;
    status: Nullable<IStatus>;
    created: number;
    avatar: Nullable<IAvatar>;
    relationship: Nullable<IRelationship>;
    about: Nullable<string>;
    developer: Nullable<boolean>;
    online: OnlineStatus;
    admin: boolean;
}
export interface ISelf extends IUser {
    authentication: string
    email: string
}
export interface IAvatar {
    animated: boolean,
    id: string
}

export class User {
    client: Client;
    _id: string;
    name: string;
    avatar: Nullable<IAvatar>;
    badges: Nullable<number>;
    relationship: Nullable<IRelationship>
    online: OnlineStatus;
    flags: Nullable<number>;
    auth?: string;
    email: string;
    messages: Nullable<Messages>;
    admin: boolean;
    constructor(client: Client, data: IUser | ISelf) {
        this.client = client;
        this._id = data.id;
        this.name = data.name;
        this.avatar = data.avatar;
        this.badges = data.badges;
        this.relationship = data.relationship;
        this.online = data.online;
        this.flags = null;
        this.auth = (data as ISelf).authentication
        this.email = (data as ISelf).email;
        this.messages = new Messages(this.client);
        this.admin = data.admin;
    }
    async addFriend(id: string) {
        try {
            let req = await this.client.req(`POST`, `/user/@me/relationships/${id}`, { Authorization: this.client.self?.auth!! }) as IUser;
            this.client.requests.set(req.id, new User(this.client, req));
            return req;
        } catch (e: any) {
            return null;
        }
    }
    async acceptFriend() {
        try {
            let friend = await this.client.req(`POST`, `/user/@me/relationships/pending/${this._id}`, { Authorization: this.client.self?.auth!! }) as IUser;
            this.client.users.set(friend.id, new User(this.client, friend));
            this.client.pending.delete(this._id);
            return friend;
        } catch (e: any) {
            return null;
        }
    }
    async denyFriend() {
        try {
            let friend = await this.client.req(`DELETE`, `/user/@me/relationships/pending/${this._id}`, { Authorization: this.client.self?.auth!! });
            this.client.pending.delete(this._id);
            return friend;
        } catch (e: any) {
            return null;
        }
    }
    async removeFriend() {
        try {
            let friend = await this.client.req('DELETE', `/user/@me/relationships/${this._id}`, { Authorization: this.client.self?.auth!! });
            this.client.users.delete(this._id);
        } catch (e: any) {
            return null;
        }
    }
    async setRelationship() { }
    async openDM() { }
    async uploadAvatar(avatar: File) {
        let form = new FormData();
        form.append('avatar', avatar, 'avatar');
        let request = new XMLHttpRequest();
        request.open('POST', `${this.client.apiURL}/user/@me/avatar`);
        //@ts-ignore
        request.setRequestHeader('Authorization', this.client.self.auth);
        request.upload.onprogress = (e: ProgressEvent) => {
            let { loaded, total } = e;
            let percentage = Math.ceil((loaded / total) * 100);
            //@ts-ignore
            this.client.emit('user/uploadavatar/progress', percentage);
        }
        request.send(form);
        request.onload = (d) => {
            let response = JSON.parse(request.responseText);
            this.avatar = response.avatar;
            //@ts-ignore
            return Promise.resolve(this.client.emit('user/uploadavatar/finish', response));
        }

    }
    async changeName(name: string) {
        try {
            let updatedUser = await this.client.req('PATCH', '/user/@me', {
                Authorization: this.client.self?.auth!!
            }, {
                name: name
            });
            this.name = updatedUser.name;
            return updatedUser.name;
        } catch (e: any) {
            return null;
        }
    }
    $update(data: Partial<IUser>) {
        const apply = (key: string) => {
            // This code has been tested.
            if (
                // @ts-expect-error TODO: clean up types here
                typeof data[key] !== "undefined" &&
                // @ts-expect-error TODO: clean up types here
                !isEqual(this[key], data[key])
            ) {
                // @ts-expect-error TODO: clean up types here
                this[key] = data[key];
            }
        }
        apply("username");
        apply("avatar");
        apply("badges");
        apply("status");
        apply("relationship");
        apply("online");
        apply("flags");
        apply("bot");
    }
    getAvatar() {
        //@ts-ignore
        return `${this.client.apiURL}/cdn/user/${this._id}/avatars/avatar_${this.avatar.id}`;
    }
}

export default class Users extends Map<string, User> {
    client: Client
    constructor(client: Client) {
        super();
        this.client = client;
        this.create.bind(this);
    }
    $get(id: string, data?: IUser) {
        const user = this.get(id)!;
        if (data) user.$update(data);
        return user;
    }
    create(data: IUser) {
        if (this.has(data.id)) return this.$get(data.id, data);
        const user = new User(this.client, data);
        //@ts-ignore
        this.client.emit('user/relationship', user);
        return user;
    }
}