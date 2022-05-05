import { AppStore } from "@/stores/AppStore";
import { IPartialSelf } from "@/stores/AuthenticationStore";
import { State } from "@/stores/ClientStore";
import { SnackStore } from "@/stores/SnackStore";
import axios, { Axios, AxiosRequestHeaders } from "axios";
import EventEmitter from "events";
import IClientStore from "@/stores/ClientStore";
import { ChattySocket, ServerReadyPayload } from "./Socket/ChattySocket";
import Logger from "./Logger";
import { Route, RouteMethod, RoutePath } from "./Routes/Routes";
import { AttachmentMeta } from "./structures/Attachment";
import { Device } from "./structures/Device";
import Messages, { IMessage } from "./structures/Messages";
import Users, { IUser, User } from "./structures/Users";
import { Nullable } from "./utils/null";
import { Release } from "./structures/Release";
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
    requests: Users;
    apiURL: string;
    store: IClientStore;
    Axios: Axios;
    ws: ChattySocket;
    gateway: string;
    logger: Logger;
    constructor(options: Partial<ClientOptions> = {}) {
        super();
        this.options = options;
        this.users = new Users(this);
        this.pending = new Users(this);
        this.requests = new Users(this);
        this.apiURL = options.rest?.api!!;
        this.store = options.store as IClientStore;
        this.Axios = axios.create({ baseURL: this.apiURL });
        this.gateway = "ws://localhost:8081";
        this.ws = new ChattySocket(this);
        this.logger = new Logger();
        this.logger.logInfo('ChattyClient', 'Init', null);
        // this.ws.on('message', (msg: any) => {
        //     let message = msg.message as IMessage;
        //     console.log(message, msg);
        //     this.users.$get(msg.user.id).messages?.create(message);
        // });
        this.ws.on('dropped', (payload: Device) => {
            this.options.store.setLoggedInDevice(payload);
        });
        this.ws.on('ready', (payload: ServerReadyPayload) => {
            console.log(this.options);
            let data = payload.d;
            // Self
            data.user.authentication = this.self.auth!!
            this.self = new User(this, data.user);
            // Relationships
            for (let i = 0; data.relationships.friends.length > i; i++) { // Friends
                let friend = data.relationships.friends[i];
                this.users.set(friend.id, new User(this, friend));
            }
            this.logger.log('ChattySocket', `Total Friends = ${data.relationships.friends.length}`, null);
            for (let i = 0; data.relationships.pending.length > i; i++) { // Pending Friends
                let pending = data.relationships.pending[i];
                this.store.setPendingUser(new User(this, pending));
            }
            this.logger.log('ChattySocket', `Total Pending Friends = ${data.relationships.pending.length}`, null);
            for (let i = 0; data.relationships.requests.length > i; i++) { // Pending Requests
                let request = data.relationships.requests[i];
                this.store.setRequestedUser(new User(this, request));
            }
            this.logger.log('ChattySocket', `Total Requests = ${data.relationships.requests.length}`, null);
            let prod = payload.d.meta.prod;
            this.options.store.setProduction(prod);
        });
        this.ws.on('pending://create', (payload: IUser) => {
            this.store.setPendingUser(new User(this, payload));
            this.logger.logInfo('ChattySocket', 'New Pending Friend', payload);
        });
        this.ws.on('friend://update', (payload: IUser) => {
            this.store.setUser(payload);
            this.logger.logInfo('ChattySocket', 'Friend Update', payload);
        })
        this.ws.on('request://accepted', (payload: IUser) => {
            this.store.setUser(payload);
            this.store.requests.delete(payload.id);
            this.logger.logSuccess('ChattySocket', 'Friend Request Accepted!', payload);
        })
        this.ws.on('release', (payload: Release) => {
            this.logger.logInfo('ChattySocket', 'New Release', payload);
            AppStore().setRelease(payload);
            
        })
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
        let login = await this.req('POST', '/user/@me/login', {
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
        let login = await this.req('GET', '/user/@me', {
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
            }).catch((err) => {
                let snackstore = SnackStore();
                snackstore.create('error', 'Websocket failed to connect', false, undefined);
                this.options.store.setNotice('Failed to Connect');
                this.options.store.setState(State.FAILED);
            });
        }

    }
    async getReleases() {
        return await this.req('GET', '/cdn/releases') as Nullable<Release[]>
    }
    async saveRelease(release: Release) {
        // @ts-ignore 
        return await this.req('POST', '/admin/release', { 'Authorization': this.self?.auth!! }, release);
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
    headers: {},
    retries: number,
    version: number,
}

export interface IMessages {
    end: string,
    messages: IMessage[]
}