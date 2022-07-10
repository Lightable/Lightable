
export interface Account {
    badges: number | null;
    id: string;
    name: string;
    status: Status | null;
    created: number;
    avatar: Avatar;
    about: Nullable<string>;
    developer: Nullable<boolean>;
    admin: boolean;
    state: UserState;
    enabled: boolean;
    profileOptions: Map<string, boolean>
}

export interface Friend {
    name: string;
    status: Status;
    admin: boolean;
    enabled: boolean;
    state: number;
    avatar: Avatar,
    id: string
}

export interface Avatar {
    animated: boolean;
    id: string;
}
export interface Status {
    icon: Icon
    text: string;
}
export interface Icon {
    cdn: string;
    animated: string;
    id: string;
}