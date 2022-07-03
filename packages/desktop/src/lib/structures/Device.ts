export interface Device {
    ip: string,
    browser: BrowserType
    build: string
    os: OSType
}

export type BrowserType = "Firefox Developer" | "Firefox" | "Firefox Nightly" | "Chrome" | "Desktop" | "Native" | string;
export type OSType = "Windows" | "MacOS" | "Linux" | "Unknown" | string;