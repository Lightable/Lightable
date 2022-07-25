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

export namespace mocks {
	
	export class AppConfig {
	    Theme: string;
	
	    static createFrom(source: any = {}) {
	        return new AppConfig(source);
	    }
	
	    constructor(source: any = {}) {
	        if ('string' === typeof source) source = JSON.parse(source);
	        this.Theme = source["Theme"];
	    }
	}

}

