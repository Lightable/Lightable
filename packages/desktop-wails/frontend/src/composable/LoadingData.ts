export interface LoadingStep {
    name: string
    finished: boolean
}

export enum LoadingStates {
    PENDING = 1,
    WARNING = 2,
    FAILED = 3,
    SUCCESS = 4
}
