export interface Actionable {
    getActionLabel(): string;
    doAction(): void;
}

export function isActionable(obj: any): obj is Actionable {
    return typeof obj.prototype.getActionLabel === 'function'
        && typeof obj.prototype.doAction === 'function'
}