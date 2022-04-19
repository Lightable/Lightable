export default interface ISocketDecoder {
    decode(data: Uint8Array | string): Promise<SocketResponse>
}

export interface SocketResponse {
    op: OPCodes,
    type?: RecieveCodes,
    d: any
}

export enum OPCodes {
    Dispatch = 0,
    Heartbeat = 1,
    Identify = 2,
    Presence = 3,
    Voice = 4,
    Resume = 5,
    Invalid = 6,
    Hello = 7,
    ACK = 8,
    Recieve = 9
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