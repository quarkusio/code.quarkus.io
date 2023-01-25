export interface Analytics {
    init(): void;
    pageview(path: string): void;

    event(event: string, params?: object): void;
}
