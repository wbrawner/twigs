package com.wbrawner.twigs.permission;

import com.wbrawner.twigs.user.User;

public class UserPermissionResponse {
    private final String user;
    private final Permission permission;

    public UserPermissionResponse(UserPermission userPermission) {
        this(userPermission.getUser(), userPermission.getPermission());
    }

    public UserPermissionResponse(User user, Permission permission) {
        this.user = user.getId();
        this.permission = permission;
    }

    public String getUser() {
        return user;
    }

    public Permission getPermission() {
        return permission;
    }
}
