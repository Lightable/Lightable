export default interface ISocketDecoder {
    decode(data: Uint8Array | string): Promise<SocketResponse>
}

export interface SocketResponse {
    t: SocketMessageType,
    d: any
}

export enum SocketMessageType {
    ClientStart,
    ServerStart,
    ClientPing,
    ServerPong,
    ClientTyping,
    ClientCallStart,
    ClientCallEnd,
    ClientStatusAway,
    ClientStatusDND,
    ClientStatusOnline,
    ServerReset,
    ServerReady,
    ServerSelfUpdate,
    ServerSessionCreate,
    ServerSessionUpdate,
    ServerSessionDelete,
    ServerPendingFriend,
    ServerFriendRemove,
    ServerSelfFriendRemove,
    ServerSelfRequestFriend,
    ServerSelfRequestRemove,
    ServerRequestRemoved,
    ServerRequestAccepted,
    ServerSelfRequestAccepted,
    ServerFriendUpdate,
    ServerMessageCreate,
    ServerSelfMessageCreate,
    ServerMessageDelete,
    ServerSelfMessageDelete,
    ServerMessageUpdate,
    ServerSelfMessageUpdate,
    ServerTyping,
    ServerSelfTyping,
    ServerDropGateway,
    ServerSelfDisabledUser,
    ServerUpdate
}
export enum RecieveCodes {
    INVALID_AUTH = -1,
    Message = 0,
    Presence = 1,
    Profile = 2,
    SELF = 3,
    Call = 4,
    Friend = 5
}
export enum FriendType {
    DENY = 0,
    ACCEPT = 1,
    PENDING = 2,
    REMOVED = 3
}
