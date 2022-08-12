export namespace mocks {
	
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
	    // Go type: Icon
	    icon: any;
	    text: string;
	
	    static createFrom(source: any = {}) {
	        return new UserStatus(source);
	    }
	
	    constructor(source: any = {}) {
	        if ('string' === typeof source) source = JSON.parse(source);
	        this.icon = this.convertValues(source["icon"], null);
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
	export class PublicUser {
	    name: string;
	    id: string;
	    // Go type: UserStatus
	    status?: any;
	    admin: boolean;
	    // Go type: UserAvatar
	    avatar?: any;
	    state?: number;
	
	    static createFrom(source: any = {}) {
	        return new PublicUser(source);
	    }
	
	    constructor(source: any = {}) {
	        if ('string' === typeof source) source = JSON.parse(source);
	        this.name = source["name"];
	        this.id = source["id"];
	        this.status = this.convertValues(source["status"], null);
	        this.admin = source["admin"];
	        this.avatar = this.convertValues(source["avatar"], null);
	        this.state = source["state"];
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
	
	    static createFrom(source: any = {}) {
	        return new RelationshipStruct(source);
	    }
	
	    constructor(source: any = {}) {
	        if ('string' === typeof source) source = JSON.parse(source);
	        this.pending = this.convertValues(source["pending"], PublicUser);
	        this.requests = this.convertValues(source["requests"], PublicUser);
	        this.friends = this.convertValues(source["friends"], PublicUser);
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
	export class PrivateUser {
	    name: string;
	    id: string;
	    email: string;
	    // Go type: UserStatus
	    status?: any;
	    // Go type: StandardToken
	    token: any;
	    admin: boolean;
	    // Go type: UserAvatar
	    avatar?: any;
	    profileOptions: {[key: string]: boolean};
	
	    static createFrom(source: any = {}) {
	        return new PrivateUser(source);
	    }
	
	    constructor(source: any = {}) {
	        if ('string' === typeof source) source = JSON.parse(source);
	        this.name = source["name"];
	        this.id = source["id"];
	        this.email = source["email"];
	        this.status = this.convertValues(source["status"], null);
	        this.token = this.convertValues(source["token"], null);
	        this.admin = source["admin"];
	        this.avatar = this.convertValues(source["avatar"], null);
	        this.profileOptions = source["profileOptions"];
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
	export class AppConfig {
	    theme: string;
	    currentUser?: PrivateUser;
	    hasUser: boolean;
	    users?: {[key: string]: PrivateUser};
	
	    static createFrom(source: any = {}) {
	        return new AppConfig(source);
	    }
	
	    constructor(source: any = {}) {
	        if ('string' === typeof source) source = JSON.parse(source);
	        this.theme = source["theme"];
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

