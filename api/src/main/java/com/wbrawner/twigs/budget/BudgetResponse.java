package com.wbrawner.twigs.budget;

import com.wbrawner.twigs.permission.UserPermission;
import com.wbrawner.twigs.permission.UserPermissionResponse;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BudgetResponse {
    public final String id;
    public final String name;
    public final String description;
    private final List<UserPermissionResponse> users;

    public BudgetResponse(String id, String name, String description, List<UserPermissionResponse> users) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.users = users;
    }

    public BudgetResponse(Budget budget, List<UserPermission> users) {
        this(
                Objects.requireNonNull(budget.getId()),
                budget.getName(),
                budget.getDescription(),
                users.stream()
                        .map(UserPermissionResponse::new)
                        .collect(Collectors.toList())
        );
    }

    public List<UserPermissionResponse> getUsers() {
        return List.copyOf(users);
    }
}
