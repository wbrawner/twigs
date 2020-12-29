package com.wbrawner.budgetserver.permission;

public class UserPermissionRequest {
    private final String user;
    private final Permission permission;

    public UserPermissionRequest() {
        this(null, Permission.READ);
    }

    public UserPermissionRequest(String user, Permission permission) {
        this.user = user;
        this.permission = permission;
    }

    public String getUser() {
        return user;
    }

    public Permission getPermission() {
        return permission;
    }
}
