import { AppStore } from "@/stores/AppStore";
import { SnackStore } from "@/stores/SnackStore";
// @ts-ignore 
import EventEmitter from "events";
import { Client, SocketCompressionType, SocketProperties } from "../Client";
import Logger from "../Logger";
import DefaultDecoder from "./_decoders/DefaultDecoder";
import ISocketDecoder, { FriendType, SocketMessageType, RecieveCodes, SocketResponse } from "./_decoders/SocketDecode";
import ZLibDecoder from "./_decoders/ZLibDecoder";
import { Device } from "../structures/Device";
import { IMessage } from "../structures/Messages";
import { IUser, ISelf, OnlineStatus } from "../structures/Users";
import { Release } from "../structures/Release";


interface ChattySocketEvents {
    'connected': () => void;
    'ready': (payload: ServerReadyPayload) => void;
    'dropped': (payload: Device) => void;
    'pong': () => void;
    'pending://create': (payload: IUser) => void;
    'error': (payload: any) => void;
    'friend://update': (payload: IUser) => void;
    'request://accepted': (payload: IUser) => void;
    'release': (payload: Release) => void;
    
}
export declare interface ChattySocket {
    on<U extends keyof ChattySocketEvents>(
        event: U, listener: ChattySocketEvents[U]
      ): this;
    
      emit<U extends keyof ChattySocketEvents>(
        event: U, ...args: Parameters<ChattySocketEvents[U]>
      ): boolean;
}
export class ChattySocket extends EventEmitter {
    client: Client;
    compression: SocketCompressionType;
    gateway: string;
    binaryType: SocketBinaryType;
    state: SocketState;
    decoder: ISocketDecoder;
    ws?: WebSocket;
    logger: Logger;
    //@ts-ignore
    failureSnack: string;
    //@ts-ignore
    reconnector: NodeJS.Timer;
    //@ts-ignore
    heartbeatTiming: Number;
    constructor(client: Client) {
        super();
        this.client = client;
        this.compression = this.client.options.socket!!.compression;
        this.gateway = `${this.client.gateway}/ws?compression=${this.compression}`;
        this.binaryType = (this.compression != "none") ? "arraybuffer" : null;
        this.state = SocketState.INITIALIZED;
        this.decoder = (this.compression = "zlib") ? new ZLibDecoder() : new DefaultDecoder();
        this.logger = new Logger();
        this.logger.logInfo('ChattySocket', 'Init', null);
    }
    async start() {
        this.ws = new WebSocket(`${this.gateway}`);
        this.logger.logInfo('ChattySocket::Start', 'Fast Connect establishing connection', this.ws);
        let snackStore = SnackStore();
        let appStore = AppStore();
        let startFastConnect = Date.now();
        if (this.binaryType) this.ws.binaryType = this.binaryType;

        return new Promise((resolve, reject) => {
            this.ws!!.onopen = () => {
                this.emit('connected');
                this.state = SocketState.CONNECTED;
                this.logger.logSuccess('ChattySocket::Start', `Fast Connect connection established in ${Date.now() - startFastConnect}ms`, this.ws);
                if (this.failureSnack) {
                    snackStore.remove(this.failureSnack);
                    snackStore.create('success', 'Back Online', true, 5000);
                }
                appStore.setOffline(false);
                resolve(this.ws);
            }
            this.ws!!.onerror = (err: Event) => {
                this.emit('error', err);
                this.state = SocketState.DISCONNECTED;
                console.log('Something went wrong', err)
                reject(this.ws);
            }
        })
    }
    async $login() {
        if (!this.ws) throw new Error("Websocket hasn't started");
        if (!this.client.options.auth) throw new Error("Client.options.auth doesn't exist yet");
        this.state = SocketState.AUTHENTICATING;
        let payload = {
            t: 0,
            d: {
                auth: this.client.options.auth,
                properties: {
                    os: 'Windows',
                    browser: 'Desktop',
                    build: '1.9.2'
                }
            }
        }
        this.ws!!.send(JSON.stringify(payload));
        this.$heartbeat();
        this.$stream();
    }
    async $heartbeat() {

        setInterval(() => {
            if (this.state == SocketState.DISCONNECTED) {
                this.logger.logWarn('ChattySocket', `Can't send 💓 to server as client is currently disconnected`, null);
                return
            } else {
                this.ws!!.send(
                    JSON.stringify({
                        t: 2,
                    })
                );
            }
        }, 30000);
    }
    $tryReconnect() {
        this.logger.logWarn('ChattySocket', 'Attempting to reconnect', null);
        this.start().then(() => {
            this.$login();
            clearInterval(this.reconnector)
        }).catch((err) => {
            console.error('Failed to reconnect', err);
        });
    }
    $stream() {
        let snackStore = SnackStore();
        let appStore = AppStore();
        if (!this.ws) throw new Error("Websocket hasn't started");
        this.ws!!.onerror = (err: Event) => {
            this.emit('error', err);
            this.state = SocketState.DISCONNECTED;
            console.log('Something went wrong', err)
        };
        this.ws!!.onclose = (e: CloseEvent) => {

            this.logger.logWarn('ChattySocket', 'Websocket closed', e);
            //@ts-ignore
            this.failureSnack = snackStore.create('error', `Disconnected from server`, false, null).id;
            if (this.state != SocketState.DROPPED) {
                this.state = SocketState.DISCONNECTED;
                this.reconnector = setInterval(() => {
                    this.$tryReconnect();
                }, 10000)
            }
            appStore.setOffline(true);
        }
        this.ws!!.onmessage = async (event: MessageEvent) => {
            let message = (this.compression == "zlib") ? await this.decoder.decode(event.data) : await this.decoder.decode(event.data);
            switch (message.t) {
                case SocketMessageType.ServerStart: {
                    let data = message as ServerStartPayload;
                    this.heartbeatTiming = data.d.interval;
                    break;
                }
                case SocketMessageType.ServerReady: {
                    let data = message as ServerReadyPayload;
                    this.emit('ready', data);
                    break;
                }
                case SocketMessageType.ServerDropGateway: {
                    let data = message.d as Device
                    this.state = SocketState.DROPPED;
                    this.emit('dropped', data);
                    break;
                }
                case SocketMessageType.ServerPong: {
                    this.emit('pong');
                    break;
                }
                case SocketMessageType.ServerPendingFriend: {
                    let data = message.d as IUser
                    console.log('PENDING', message);
                    this.emit('pending://create', data);
                    break;
                }
                case SocketMessageType.ServerFriendUpdate: {
                    let data = message.d.user as IUser
                    this.emit('friend://update', data);
                    break;
                }
                case SocketMessageType.ServerRequestAccepted: {
                    let data = message.d as IUser
                    this.emit('request://accepted', data)
                }
                case SocketMessageType.ServerUpdate: {
                    let release = message.d as Release
                    this.emit('release', release);
                    console.log('CHATSOCK', release);
                    break;
                }
                // case OPCodes.Heartbeat: {
                //     this.emit('internal://heartbeat');
                //     break;
                // }
                // case OPCodes.Identify: {
                //     this.emit('internal://identify');
                //     break;
                // }
                // case OPCodes.Presence: {
                //     this.emit('presence', message.d.user);
                //     break;
                // }
                // case OPCodes.Voice: {
                //     this.emit('internal://voice');
                //     break;
                // }
                // case OPCodes.Resume: {
                //     this.emit('internal://resume');
                //     break;
                // }
                // case OPCodes.Invalid: {
                //     this.emit('internal://invalid');
                //     break;
                // }
                // case OPCodes.Hello: {
                //     this.emit('internal://hello');
                //     break;
                // }
                // case OPCodes.ACK: {
                //     this.emit('internal://ack');
                //     break;
                // }
                // case OPCodes.Recieve: {
                //     this.emit('internal://recieve', message.d);
                //     switch (message.type!!) {
                //         case RecieveCodes.INVALID_AUTH: {
                //             this.emit('invalid', message.d.expiry);
                //             break;
                //         }
                //         case RecieveCodes.Message: {
                //             console.log('MESSAGE', message);
                //             this.emit('message', {
                //                 user: message.d.user,
                //                 message: message.d.message
                //             });
                //             break;
                //         }
                //         case RecieveCodes.Presence: {
                //             this.emit('presence', {
                //                 id: message.d.id,
                //                 status: message.d.status,
                //                 online: message.d.online
                //             })
                //             break;
                //         }
                //         case RecieveCodes.SELF: {
                //             this.emit('internal://selfmessage', {
                //                 user: message.d.user,
                //                 message: message.d.message
                //             });
                //             break;
                //         }
                //         case RecieveCodes.Friend: {
                //             switch (message.d.ft) {
                //                 case FriendType.DENY: {
                //                     if (message.d.ft == 0) {
                //                         this.emit('friend://ext/deny', message.d.friend);
                //                     } else {
                //                         this.emit('friend://int/deny', message.d.user);
                //                     }
                //                     break;
                //                 }
                //                 case FriendType.ACCEPT: {
                //                     if (message.d.ft == 1) {
                //                         this.emit('friend://ext/accept', message.d.friend);
                //                     } else {
                //                         this.emit('friend://int/accept', message.d.user);
                //                     }
                //                     break;
                //                 }
                //                 case FriendType.PENDING: {
                //                     if (message.d.ft == 2) {
                //                         this.emit('friend://ext/pending', message.d.user);
                //                     } else {
                //                         this.emit('friend://int/pending');
                //                     }
                //                     break;
                //                 }
                //                 case FriendType.REMOVED: {
                //                     if (message.d.ft == 3) {
                //                         this.emit('friend://ext/removed', message.d.user);
                //                     } else {
                //                         this.emit('friend://int/removed', message.d.user);
                //                     }
                //                     break;
                //                 }
                //             }
                //         }
                //     }
                //     break;
                // }
            }
        }
    }
}

export type SocketBinaryType = "arraybuffer" | "blob" | null;
export enum SocketState {
    INITIALIZED = 0,
    DISCONNECTED = 1,
    AUTHENTICATING = 2,
    CONNECTED = 3,
    DROPPED = 4,
}

export interface SocketLogin {
    t: number,
    auth: string,
    properties: SocketProperties
}
export interface SocketAuthenticated extends SocketResponse {
    user: string
}
export interface SocketPresence extends SocketResponse {
    user: IUser
}
export interface SocketMessage extends SocketResponse {
    user: IUser
    message: IMessage
}
export interface SocketPresence extends SocketResponse {
    id: string,
    status?: SocketStatus
    online: OnlineStatus
}

export interface SocketStatus extends SocketResponse {
    text: string;
    icon: string;
}

export class ServerStartPayload {
    //@ts-ignore
    t: SocketMessageType.ServerStart
    //@ts-ignore
    d: {
        interval: number
    }
}

export class ServerReadyPayload {
    //@ts-ignore
    t: SocketMessageType.ServerReady
    //@ts-ignore
    d: {
        user: ISelf,
        relationships: {
            friends: IUser[],
            pending: IUser[],
            requests: IUser[]
        },
        meta: {
            prod: boolean
        }
    }
}