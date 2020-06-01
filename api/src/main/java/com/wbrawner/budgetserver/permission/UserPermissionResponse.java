package com.wbrawner.budgetserver.permission;

import com.wbrawner.budgetserver.user.UserResponse;

public class UserPermissionResponse {
    private final UserResponse user;
    private final Permission permission;

    public UserPermissionResponse(UserPermission userPermission) {
        this(new UserResponse(userPermission.getUser()), userPermission.getPermission());
    }

    public UserPermissionResponse(UserResponse userResponse, Permission permission) {
        this.user = userResponse;
        this.permission = permission;
    }

    public UserResponse getUser() {
        return user;
    }

    public Permission getPermission() {
        return permission;
    }
}
