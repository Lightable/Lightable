export namespace mocks {
	
	export class UserAnalytics {
	    messages: number;
	    callTime: number;
	    activeTime: number;
	
	    static createFrom(source: any = {}) {
	        return new UserAnalytics(source);
	    }
	
	    constructor(source: any = {}) {
	        if ('string' === typeof source) source = JSON.parse(source);
	        this.messages = source["messages"];
	        this.callTime = source["callTime"];
	        this.activeTime = source["activeTime"];
	    }
	}
	export class UserAvatar {
	    animated: boolean;
	    id: string;
	
	    static createFrom(source: any = {}) {
	        return new UserAvatar(source);
	    }
	
	    constructor(source: any = {}) {
	        if ('string' === typeof source) source = JSON.parse(source);
	        this.animated = source["animated"];
	        this.id = source["id"];
	    }
	}
	export class StandardToken {
	    permissions: string[];
	    token: string;
	
	    static createFrom(source: any = {}) {
	        return new StandardToken(source);
	    }
	
	    constructor(source: any = {}) {
	        if ('string' === typeof source) source = JSON.parse(source);
	        this.permissions = source["permissions"];
	        this.token = source["token"];
	    }
	}
	export class Icon {
	    cdn: string;
	    animated: string;
	    id: string;
	
	    static createFrom(source: any = {}) {
	        return new Icon(source);
	    }
	
	    constructor(source: any = {}) {
	        if ('string' === typeof source) source = JSON.parse(source);
	        this.cdn = source["cdn"];
	        this.animated = source["animated"];
	        this.id = source["id"];
	    }
	}
	export class UserStatus {
	    icon: Icon;
	    text: string;
	
	    static createFrom(source: any = {}) {
	        return new UserStatus(source);
	    }
	
	    constructor(source: any = {}) {
	        if ('string' === typeof source) source = JSON.parse(source);
	        this.icon = this.convertValues(source["icon"], Icon);
	        this.text = source["text"];
	    }
	
		convertValues(a: any, classs: any, asMap: boolean = false): any {
		    if (!a) {
		        return a;
		    }
		    if (a.slice) {
		        return (a as any[]).map(elem => this.convertValues(elem, classs));
		    } else if ("object" === typeof a) {
		        if (asMap) {
		            for (const key of Object.keys(a)) {
		                a[key] = new classs(a[key]);
		            }
		            return a;
		        }
		        return new classs(a);
		    }
		    return a;
		}
	}
	export class PrivateUser {
	    name: string;
	    id: string;
	    email: string;
	    status?: UserStatus;
	    token: StandardToken;
	    admin: boolean;
	    avatar?: UserAvatar;
	    profileOptions: {[key: string]: boolean};
	    analytics?: UserAnalytics;
	
	    static createFrom(source: any = {}) {
	        return new PrivateUser(source);
	    }
	
	    constructor(source: any = {}) {
	        if ('string' === typeof source) source = JSON.parse(source);
	        this.name = source["name"];
	        this.id = source["id"];
	        this.email = source["email"];
	        this.status = this.convertValues(source["status"], UserStatus);
	        this.token = this.convertValues(source["token"], StandardToken);
	        this.admin = source["admin"];
	        this.avatar = this.convertValues(source["avatar"], UserAvatar);
	        this.profileOptions = source["profileOptions"];
	        this.analytics = this.convertValues(source["analytics"], UserAnalytics);
	    }
	
		convertValues(a: any, classs: any, asMap: boolean = false): any {
		    if (!a) {
		        return a;
		    }
		    if (a.slice) {
		        return (a as any[]).map(elem => this.convertValues(elem, classs));
		    } else if ("object" === typeof a) {
		        if (asMap) {
		            for (const key of Object.keys(a)) {
		                a[key] = new classs(a[key]);
		            }
		            return a;
		        }
		        return new classs(a);
		    }
		    return a;
		}
	}
	export class AppResponderConfig {
	    gateway: string;
	    api: string;
	    secure: boolean;
	
	    static createFrom(source: any = {}) {
	        return new AppResponderConfig(source);
	    }
	
	    constructor(source: any = {}) {
	        if ('string' === typeof source) source = JSON.parse(source);
	        this.gateway = source["gateway"];
	        this.api = source["api"];
	        this.secure = source["secure"];
	    }
	}
	export class AppConfig {
	    theme: string;
	    responder?: AppResponderConfig;
	    currentUser?: PrivateUser;
	    hasUser: boolean;
	    users?: {[key: string]: PrivateUser};
	
	    static createFrom(source: any = {}) {
	        return new AppConfig(source);
	    }
	
	    constructor(source: any = {}) {
	        if ('string' === typeof source) source = JSON.parse(source);
	        this.theme = source["theme"];
	        this.responder = this.convertValues(source["responder"], AppResponderConfig);
	        this.currentUser = this.convertValues(source["currentUser"], PrivateUser);
	        this.hasUser = source["hasUser"];
	        this.users = source["users"];
	    }
	
		convertValues(a: any, classs: any, asMap: boolean = false): any {
		    if (!a) {
		        return a;
		    }
		    if (a.slice) {
		        return (a as any[]).map(elem => this.convertValues(elem, classs));
		    } else if ("object" === typeof a) {
		        if (asMap) {
		            for (const key of Object.keys(a)) {
		                a[key] = new classs(a[key]);
		            }
		            return a;
		        }
		        return new classs(a);
		    }
		    return a;
		}
	}
	
	
	
	export class Message {
	    content?: string;
	    system: boolean;
	    type: number;
	    channel: number;
	    author: number;
	    created: number;
	    edited?: number;
	
	    static createFrom(source: any = {}) {
	        return new Message(source);
	    }
	
	    constructor(source: any = {}) {
	        if ('string' === typeof source) source = JSON.parse(source);
	        this.content = source["content"];
	        this.system = source["system"];
	        this.type = source["type"];
	        this.channel = source["channel"];
	        this.author = source["author"];
	        this.created = source["created"];
	        this.edited = source["edited"];
	    }
	}
	export class PublicUser {
	    name: string;
	    id: string;
	    status?: UserStatus;
	    admin: boolean;
	    avatar?: UserAvatar;
	    state?: number;
	    channel?: Channel;
	
	    static createFrom(source: any = {}) {
	        return new PublicUser(source);
	    }
	
	    constructor(source: any = {}) {
	        if ('string' === typeof source) source = JSON.parse(source);
	        this.name = source["name"];
	        this.id = source["id"];
	        this.status = this.convertValues(source["status"], UserStatus);
	        this.admin = source["admin"];
	        this.avatar = this.convertValues(source["avatar"], UserAvatar);
	        this.state = source["state"];
	        this.channel = this.convertValues(source["channel"], Channel);
	    }
	
		convertValues(a: any, classs: any, asMap: boolean = false): any {
		    if (!a) {
		        return a;
		    }
		    if (a.slice) {
		        return (a as any[]).map(elem => this.convertValues(elem, classs));
		    } else if ("object" === typeof a) {
		        if (asMap) {
		            for (const key of Object.keys(a)) {
		                a[key] = new classs(a[key]);
		            }
		            return a;
		        }
		        return new classs(a);
		    }
		    return a;
		}
	}
	export class Channel {
	    id: string;
	    type: number;
	    users: number[];
	    owner?: PublicUser;
	    messages: Message[];
	
	    static createFrom(source: any = {}) {
	        return new Channel(source);
	    }
	
	    constructor(source: any = {}) {
	        if ('string' === typeof source) source = JSON.parse(source);
	        this.id = source["id"];
	        this.type = source["type"];
	        this.users = source["users"];
	        this.owner = this.convertValues(source["owner"], PublicUser);
	        this.messages = this.convertValues(source["messages"], Message);
	    }
	
		convertValues(a: any, classs: any, asMap: boolean = false): any {
		    if (!a) {
		        return a;
		    }
		    if (a.slice) {
		        return (a as any[]).map(elem => this.convertValues(elem, classs));
		    } else if ("object" === typeof a) {
		        if (asMap) {
		            for (const key of Object.keys(a)) {
		                a[key] = new classs(a[key]);
		            }
		            return a;
		        }
		        return new classs(a);
		    }
		    return a;
		}
	}
	
	
	
	
	
	export class RelationshipStruct {
	    pending: PublicUser[];
	    requests: PublicUser[];
	    friends: PublicUser[];
	    empty: boolean;
	
	    static createFrom(source: any = {}) {
	        return new RelationshipStruct(source);
	    }
	
	    constructor(source: any = {}) {
	        if ('string' === typeof source) source = JSON.parse(source);
	        this.pending = this.convertValues(source["pending"], PublicUser);
	        this.requests = this.convertValues(source["requests"], PublicUser);
	        this.friends = this.convertValues(source["friends"], PublicUser);
	        this.empty = source["empty"];
	    }
	
		convertValues(a: any, classs: any, asMap: boolean = false): any {
		    if (!a) {
		        return a;
		    }
		    if (a.slice) {
		        return (a as any[]).map(elem => this.convertValues(elem, classs));
		    } else if ("object" === typeof a) {
		        if (asMap) {
		            for (const key of Object.keys(a)) {
		                a[key] = new classs(a[key]);
		            }
		            return a;
		        }
		        return new classs(a);
		    }
		    return a;
		}
	}

}

export namespace app {
	
	export class CustomMemoryStats {
	    totalAlloc: number;
	    alloc: number;
	    sysAlloc: number;
	    heapSpace: number;
	    gcTotal: number;
	
	    static createFrom(source: any = {}) {
	        return new CustomMemoryStats(source);
	    }
	
	    constructor(source: any = {}) {
	        if ('string' === typeof source) source = JSON.parse(source);
	        this.totalAlloc = source["totalAlloc"];
	        this.alloc = source["alloc"];
	        this.sysAlloc = source["sysAlloc"];
	        this.heapSpace = source["heapSpace"];
	        this.gcTotal = source["gcTotal"];
	    }
	}

}

export namespace client {
	
	export class HttpResponse {
	    status: number;
	    Json: string;
	    Err: string;
	
	    static createFrom(source: any = {}) {
	        return new HttpResponse(source);
	    }
	
	    constructor(source: any = {}) {
	        if ('string' === typeof source) source = JSON.parse(source);
	        this.status = source["status"];
	        this.Json = source["Json"];
	        this.Err = source["Err"];
	    }
	}

}

