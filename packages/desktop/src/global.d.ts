export declare global {
    /**
     * The global window object
     * @param __PROD__ Is the app running in production
     */
    interface Window {
        __PROD__: boolean;
        addScript(script: string): void;
    }
    /**
     * @param logs The logs for the console
     */
    interface console {
        logs: Array<string>
    }
}