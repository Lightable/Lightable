import { AppStore } from "@/stores/AppStore";
import { IPartialSelf } from "@/stores/AuthenticationStore";
import { State } from "@/stores/ClientStore";
import { SnackStore } from "@/stores/SnackStore";
import axios, { Axios, AxiosRequestHeaders } from "axios";
import EventEmitter from "events";
import { Store } from "pinia";
import { ChattySocket } from "./ChattySocket";
import Logger from "./Logger";
import { Route, RouteMethod, RoutePath } from "./Routes/Routes";
import { AttachmentMeta } from "./structures/Attachment";
import Messages, { IMessage } from "./structures/Messages";
import Users, { User } from "./structures/Users";
import { Nullable } from "./util/null";
export declare interface Client {
    on(event: 'connecting', listener: () => void): this
    on(event: 'connected', listener: () => void): this
    on(event: 'disconnected', listener: () => void): this
    on(event: 'ready', listener: () => void): this
    on(event: 'logout', listener: () => void): this
    on(event: 'message', listener: () => void): this
}
export class Client extends EventEmitter {
    options: Partial<ClientOptions>;
    self?: User;
    users: Users;
    pending: Users;
    apiURL: string;
    Axios: Axios;
    ws: ChattySocket;
    gateway: string;
    logger: Logger;
    constructor(options: Partial<ClientOptions> = {}) {
        super();
        this.options = options;
        this.users = new Users(this);
        this.pending = new Users(this);
        this.apiURL = options.rest?.api!!;
        this.Axios = axios.create({ baseURL: this.apiURL });
        this.gateway = "ws://localhost:5898";
        this.ws = new ChattySocket(this);
        this.logger = new Logger();
        this.logger.logInfo('ChattyClient', 'Init', null);
        this.ws.on('message', (msg: any) => {
            let message = msg.message as IMessage;
            console.log(message, msg);
            this.users.$get(msg.user.id).messages?.create(message);
        });
    }
    async req<M extends RouteMethod, T extends RoutePath>(
        method: M,
        url: T | string,
        headers?: Record<string, string | number | boolean>,
        data?: Route<M, T>["data"],
    ): Promise<Route<M, T>["response"]> {
        let snackstore = SnackStore();
        let appStore = AppStore();
        try {
            // @ts-ignore 
            const res = await this.Axios.request({
                method,
                data,
                headers,
                url,
            });
            if (appStore.debug) {
                snackstore.create('info', `Request given ${method}, ${url}`, true, 2000);
            }
            return res.data;
        } catch (e: any) {
            if (appStore.debug) {
                snackstore.create('error', `${e.message}<br>${JSON.stringify(e.response.data)}`, true, 2000);
            }
            return null;
        }
    }
    /**
     * Login using email and password
     * @param {String} email Account email
     * @param {String} password Account password
     */
    async loginWEP(email: string, password: string) {
        if (!email || !password) throw new SyntaxError('Email, nor password can be null');
        let login = await this.req('POST', '/v2/user/@me/login', {
            email: email,
            password: password
        })
        if (!login) {
            throw new LoginFailure('Login failed', { email, password });
        }
    }
    /**
     * Login using Token
     * @param {String} auth Account Token
     */
    async loginWT(auth: string) {
        if (!auth) throw new SyntaxError("Token can't be null");
        this.options.auth = auth;
        this.options.store.setState(State.SUCCESS);
        this.options.store.setNotice('Logging In');
        let login = await this.req('GET', '/v2/user/@me', {
            'Authorization': auth
        });
        login.authentication = auth;
        if (!login) {
            throw new LoginFailure('Login failed', {});
        } else {
            this.self = new User(this, login);
            this.options.store.setNotice('Connecting to WebSocket');
            this.ws.start().then(() => {
                this.options.store.setNotice('Authenticating');
                this.ws.$login();
                this.options.store.setState(State.FINISHED);
                this.getFriends();
            }).catch((err) => {
                let snackstore = SnackStore();
                snackstore.create('error', 'Websocket failed to connect', false, undefined);
                this.options.store.setNotice('Failed to Connect');
                this.options.store.setState(State.FAILED);
            });
        }

    }
    async getFriends() {
        let friendRequest = await this.req('GET', '/v2/user/@me/relationships', {
            'Authorization': `${this.options.auth}`
        });
        for (let i: number = 0; friendRequest.friends.length > i; i++) {
            let friend = friendRequest.friends[i];
            let user = new User(this, friend);
            let lastMessage = await this.req('GET', `/v2/user/@me/channels/${friend.id}/messages?limit=1`, {
                'Authorization': this.self?.auth!!
            }) as Nullable<IMessages>;
            if (lastMessage) {
                user.messages?.create(lastMessage.messages[0]);
            }
            this.users.set(friend.id, user);
        }
        for (let i: number = 0; friendRequest.pending.length > i; i++) {
            let pending = friendRequest.pending[i];
            this.pending.set(pending.id, new User(this, pending));
        }
    }
    async $connect() {
        await this
    }


    generateFileURL(allowAnimation: boolean = false, fallback: boolean = true, attachment?: { id: string, animated: boolean }): string | null {
        let animation = (allowAnimation) ? 'gif' : 'png';
        return `${this.apiURL}/cdn/${this.self?._id}/${attachment?.id}.${animation}`;
    }
}
export class LoginFailure extends Error {
    constructor(message: string, data: object) {
        super(`LoginFailure: ${message} With data ${JSON.stringify(data)}`);
    }
}

export interface ClientOptions {
    store: any,
    socket: SocketOptions,
    rest: RestOptions,
    auth: string
}
export type SocketCompressionType = "zlib" | "zip" | "none";
export type SocketRegion = "US" | "UK";

export interface SocketProperties {
    os: string,
    browser: string,
    build: string
}
export interface SocketOptions {
    compression: SocketCompressionType,
    region: SocketRegion,
    account: IPartialSelf | null,
    properties: SocketProperties
}
export interface RestOptions {
    api: string,
    cdn: string,
    headers: {},
    retries: number,
    version: number,
}

export interface IMessages {
    end: string,
    messages: IMessage[]
}