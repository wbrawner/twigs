
export enum Permission {
    READ = "READ",
    WRITE = "WRITE",
    MANAGE = "MANAGE",
    OWNER = "OWNER"
};

export function isAtLeast(permission: Permission, min: Permission): boolean {
    switch (permission) {
        case Permission.OWNER:
            return true;
        case Permission.MANAGE:
            return min !== Permission.OWNER;
        case Permission.WRITE:
            return min === Permission.READ || min === Permission.WRITE;
        case Permission.READ:
            return min === Permission.READ;
    }
}

export class UserPermission {
    user: string;
    permission: Permission;
}