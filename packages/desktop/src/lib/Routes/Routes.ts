import { ISelf, IUser, User } from "../structures/Users";
import { Nullable } from "../utils/null";
import { Device } from "./interfaces/Devices";
import { Release } from "./interfaces/Releases";
import { UserCreate } from "./interfaces//UserCreate";
import { UserLogin } from "./interfaces/UserLogin";

type ReleaseTag = "0.0.1";
type FriendID = '';
type Routes =
    /* Release */
    | {
        method: "GET";
        route: `/release/latest`;
        data: undefined;
        response: Release;
    }
    | {
        method: 'GET';
        route: `/release/${ReleaseTag}`;
        data: undefined;
        response: Nullable<Release>;
    }
    | {
        method: 'GET';
        route: `/releases`;
        data: undefined;
        response: Nullable<Release[]>;
    }
    /* Device */
    | {
        method: 'GET';
        route: `/user/@me/devices`;
        data: undefined;
        response: Nullable<Device[]>;
    }
    /* User */
    | {
        method: 'POST',
        route: `/user/@me/create`;
        data: UserCreate;
        response: User
    }
    | {
        method: 'POST',
        route: `/user/@me/login`;
        data: Nullable<UserLogin>;
        response: ISelf
    }
    | {
        method: 'PATCH',
        route: '/user/@me';
        data: {
            name?: string;
            status?: object;
            avatar?: string;
            about?: string;
            enabled?: boolean
        }
        response: ISelf
    }
    | {
        method: 'GET';
        route: '/user/@me';
        data: undefined;
        response: ISelf
    }
    | {
        method: 'PUT',
        route: `/user/@me/relationships/${FriendID}`,
        data: undefined,
        response: Nullable<PendingFriend>
    } | {
        method: 'GET',
        route: '/user/@me/relationships',
        data: undefined,
        response: Relationships
    }
    | {
        method: 'POST',
        route: '/user/@me/relationships/pending/id/accept',
        data: undefined,
        response: IUser
    }
    | {
        method: 'DELETE',
        route: '/user/@me/relationships/pending/id/deny',
        data: undefined,
        response: null
    }
    export interface PendingFriend {
        pending: boolean
    }
    export interface Relationships {
        pending: IUser[],
        friends: IUser[],
    }
export type RoutePath = Routes["route"];
export type RouteMethod = Routes["method"];
type ExcludeRouteKey<K> = K extends "route" ? never : K;
type ExcludeRouteField<A> = { [K in ExcludeRouteKey<keyof A>]: A[K] };
type ExtractRouteParameters<A, T> = A extends { route: T }
    ? ExcludeRouteField<A>
    : never;

type ExcludeMethodKey<K> = K extends "method" ? never : K;
type ExcludeMethodField<A> = { [K in ExcludeMethodKey<keyof A>]: A[K] };
type ExtractMethodParameters<A, T> = A extends { method: T }
    ? ExcludeMethodField<A>
    : never;
export type Route<
    M extends RouteMethod,
    T extends RoutePath,
    > = ExtractMethodParameters<ExtractRouteParameters<Routes, T>, M>;