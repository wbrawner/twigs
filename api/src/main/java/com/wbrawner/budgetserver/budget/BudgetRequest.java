package com.wbrawner.budgetserver.budget;

import com.wbrawner.budgetserver.permission.UserPermissionRequest;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

public class BudgetRequest {
    public final String name;
    public final String description;
    private final Set<UserPermissionRequest> users = new HashSet<>();

    public BudgetRequest(String name, String description, Set<UserPermissionRequest> users) {
        this.name = name;
        this.description = description;
        this.users.addAll(users);
    }

    @NotNull
    public Set<UserPermissionRequest> getUsers() {
        return Set.copyOf(users);
    }
}
