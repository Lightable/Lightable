import { PartialSelfUser } from "@/stores/AuthenticationStore";
import { Client } from "../Client";
import { Nullable } from "../util/null";
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
}
export interface ISelf extends IUser {
    authentication: string
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
    messages: Nullable<Messages>;
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
        this.messages = new Messages(this.client);
    }
    async addFriend(id: string) {
        console.log(this);
        try {
            return await this.client.req(`PUT`, `/v2/user/@me/relationships/${id}`, { Authorization: this.client.self?.auth!! });
        } catch (e: any) {
            return null;
        }
    }
    async acceptFriend() {
        try {
            let friend = await this.client.req(`POST`, `/v2/user/@me/relationships/pending/${this._id}/accept`, { Authorization: this.client.self?.auth!! }) as IUser;
            this.client.users.set(friend.id, new User(this.client, friend));
            this.client.pending.delete(this._id);
            return friend;
        } catch (e: any) {
            return null;
        }
    }
    async denyFriend() {
        try {
            let friend = await this.client.req(`DELETE`, `/v2/user/@me/relationships/pending/${this._id}/deny`, { Authorization: this.client.self?.auth!! });
            this.client.pending.delete(this._id);
            return friend;
        } catch (e: any) {
            return null;
        }
    }
    async removeFriend() { 
        try {
            let friend = await this.client.req('DELETE', `/v2/user/@me/relationships/${this._id}`, { Authorization: this.client.self?.auth!! });
            console.log(friend);
            this.client.users.delete(this._id);
        } catch(e: any) {
            return null;
        }
    }
    async setRelationship() { }
    async openDM() { }

    async changeName(name: string) {
        try {
            let updatedUser = await this.client.req('PATCH', '/user/@me', {
                Authorization: this.client.self?.auth!!
            }, {
                name: name
            });
            this.$update(updatedUser);
            return updatedUser.name;
        } catch(e: any) {
           return null;
        }
    }
    $update(data: Partial<IUser>) {
        for (const update in data) {
            console.log(update)
            const key = update as keyof IUser;
            (this[key] as any) = data[key];
        }
    }
    getAvatar() {
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
        this.client.emit('user/relationship', user);
        return user;
    }
}