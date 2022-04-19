import { Client } from "../Client";
import { Nullable } from "../util/null";
import { Attachment } from "./Attachment";
import { User } from "./Users";

export class Channel {
    client: Client;
    _id: string;
    active: Nullable<boolean> = null;
    name: Nullable<string> = null;
    icon: Nullable<Attachment> = null;
    typing: Set<string> = new Set();
    recipient: Nullable<User> = null;
    constructor(client: Client, data: User) {
        this.client = client;
        this._id = data._id;
        this.name = data.username;
        this.icon = data.avatar;
        this.recipient = data;
    }
    

}