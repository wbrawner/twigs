package com.wbrawner.budgetserver.permission;

public class UserPermissionRequest {
    private final Long user;
    private final Permission permission;

    public UserPermissionRequest() {
        this(0L, Permission.READ);
    }

    public UserPermissionRequest(Long user, Permission permission) {
        this.user = user;
        this.permission = permission;
    }

    public Long getUser() {
        return user;
    }

    public Permission getPermission() {
        return permission;
    }
}
