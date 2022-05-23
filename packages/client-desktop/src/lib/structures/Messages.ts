import { Client } from "../Client";
import { Nullable } from "../utils/null";
import { Attachment } from "./Attachment";
import { IUser, User } from "./Users";
import { generateParagraph } from "../utils/generateLoremIpsum";
import moment from 'moment'
export class Message {
    client: Client;
    _id: string;
    channel_id: string;
    author_id: string;
    content: string;
    attachments: Nullable<Attachment[]>;
    edited: Nullable<Date>;
    mentions: Nullable<string[]>;
    created: Date;
    user: IUser;
    readableTime: String;
    constructor(client: Client, data: IMessage) {
        this.client = client;
        this._id = data.id;
        this.channel_id = data.user.id;
        this.author_id = data.user.id;
        this.content = data.content;
        this.attachments = data.attachments;
        this.edited = new Date(String(data.edited));
        this.mentions = data.mentions;
        this.created = new Date(data.created.milli);
        this.user = data.user;
        moment.defineLocale('en', {
            relativeTime: {
                future: 'in %s',
                past: '%s ago',
                s: '1s',
                ss: '%ss',
                m: '1m',
                mm: '%dm',
                h: '1h',
                hh: '%dh',
                d: '1d',
                dd: '%dd',
                M: '1M',
                MM: '%dM',
                y: '1Y',
                yy: '%dY'
            }
        })
        this.readableTime = String(moment(this.created).fromNow());
    }
    revaluateDate() {
        this.readableTime = String(moment(this.created).fromNow());
    }
    getUser() {
        if (this.author_id == this.client.self?._id) return this.client.self?._id;
        return this.client.store.users.get(this.author_id);
    }
    get channel() {
        return
    }
    update(data: Partial<IMessage>) {
        const apply = (
            key: string,
            target?: string,
            transform?: (obj: unknown) => unknown,
        ) => {
            // This code has been tested.
            if (
                // @ts-expect-error TODO: clean up types here
                typeof data[key] !== "undefined" &&
                // @ts-expect-error TODO: clean up types here
                !isEqual(this[target ?? key], data[key])
            ) {
                // @ts-expect-error TODO: clean up types here
                this[target ?? key] = transform
                    ? // @ts-expect-error TODO: clean up types here
                    transform(data[key])
                    : // @ts-expect-error TODO: clean up types here
                    data[key];
            }
        };

        apply("content");
        apply("attachments");
        apply("edited");
        apply("embeds");
        apply("mentions", "mention_ids");
    }
}
export interface IMessage {
    content: string,
    created: ICreated,
    id: string,
    attachments: Nullable<Attachment[]>,
    mentions: Nullable<string[]>,
    edited: Nullable<number>,
    user: IUser
}
export interface ICreated {
    sec: number,
    milli: number
}
export type UIMessageType = "Classic" | "SMS"
export default class Messages extends Map<string, Message> {
    client: Client;
    constructor(client: Client) {
        super();
        this.client = client;
    }
    $get(id: string, data?: IMessage) {
        const msg = this.get(id)!
        if (data) msg.update(data);
        return msg;
    }
    create(data: IMessage) {
        if (this.has(data.id)) return this.$get(data.id);
        const message = new Message(this.client, data);
        this.set(data.id, message);
        this.client.emit('message', message);
    }
    $hasChain(id: string) {
        let currentMessage = this.$get(id)
        let findNext = Array.from(this.keys()).indexOf(id) + 1
        let next = Array.from(this.values())[findNext]
        if (!next) return false
        if (next.user.id == currentMessage.author_id) {
            return true
        } else {
            return false
        }
    }
    $makeRandom(user: string, amount: number = 1) {
        for (let i = 0; amount > i; i++) {
            const message = new Message(this.client, {
                content: generateParagraph(3),
                created: {
                    sec: 11111,
                    milli: 92,
                },
                id: this.randomString(20),
                attachments: null,
                mentions: null,
                edited: null,
                user: Array.from(this.values())[0].user
            })

            this.set(message._id, message);
        }
    }
    get last() {
        let getLastItemInMap = Array.from(this)[this.size - 1]
        return getLastItemInMap;
    }
    randomString(strLength: number) {
        var result = [];

        strLength = strLength || 5;
        let charSet = '0123456789';

        while (strLength--) { // (note, fixed typo)
            result.push(charSet.charAt(Math.floor(Math.random() * charSet.length)));
        }

        return result.join('');
    }
}