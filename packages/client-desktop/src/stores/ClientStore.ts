import { Client } from "@/lib/Client";
import Users, { IUser, User } from "@/lib/structures/Users";
import Messages, { IMessage, Message } from "@/lib/structures/Messages";
import { Nullable } from "@/lib/util/null";
import { defineStore } from "pinia";
import { IPartialSelf, PartialSelfUser } from "./AuthenticationStore";
import Logger from "@/lib/Logger";
import { Device } from "@/lib/structures/Device";

export const ClientStore = defineStore('ClientStore', {
    state: () => ({
        client: null as Nullable<Client>,
        account: null as Nullable<PartialSelfUser>,
        users: null as Nullable<Users>,
        messages: new Map<User, Messages>(),
        // Loading settings
        state: State.SUCCESS,
        notice: 'Connecting to WebSocket' as ClientNotice | string,
        logger: new Logger(),
        production: true as boolean,
        connectedDevice: null as Device | null
    }),
    actions: {
        setClient(client: Client) {
            this.client = client;
            this.users = client.users;
            this.logger.logInfo('ClientStore', 'SetClient', client);
        },
        createUser(user: IUser | User) {
            if (user instanceof User) {
                this.users?.set(user._id, user);
            } else {
                let instanceUser = this.users?.create(user)!!;
                this.users?.set(instanceUser._id, instanceUser);
            }
            this.logger.logInfo('ClientStore', 'CreateUser', user);
        },
        // createMessage(message: IMessage | Message) {
        //     if (message instanceof Message) {
        //         this.messages.set(message.user, )
        //     }
        // }
        setState(state: State) {
            this.state = state;
            this.logger.logInfo('ClientStore', 'SetState', state);
        },
        setNotice(notice: ClientNotice | string) {
            this.notice = notice;
            if (this.notice == "Failed to Connect") { this.logger.logWarn('ClientStore', 'SetNotice', this.notice) } else { this.logger.logInfo('ClientStore', 'SetNotice', this.notice) }
        },
        setProduction(production: boolean) {
            this.production = production;
        },
        setLoggedInDevice(device: Device) {
            this.connectedDevice = device;
        },
        $resetData() {
            this.account = null;
            this.connectedDevice = null;
            this.production = true;
        }
    }
})


export enum State {
    FAILED = -1,
    WARNING = 0,
    SUCCESS = 1,
    FINISHED = 2,
}

export type ClientNotice = 'Logging In' | 'Authenticating' | 'Connecting to WebSocket' | 'Getting Settings' | 'Failed to Connect'
