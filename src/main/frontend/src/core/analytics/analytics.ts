export interface Analytics {
    init(): void;
    pageview(path: string): void;
    event(category: string, action: string, label?: string, value?: number, params?: object): void;
}
