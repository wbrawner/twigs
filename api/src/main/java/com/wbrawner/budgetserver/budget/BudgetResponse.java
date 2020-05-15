package com.wbrawner.budgetserver.budget;

import com.wbrawner.budgetserver.permission.UserPermission;
import com.wbrawner.budgetserver.permission.UserPermissionResponse;
import com.wbrawner.budgetserver.user.User;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BudgetResponse {
    public final long id;
    public final String name;
    public final String description;
    private final List<UserPermissionResponse> users;

    public BudgetResponse(long id, String name, String description, List<UserPermissionResponse> users) {
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
