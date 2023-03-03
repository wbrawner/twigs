package com.wbrawner.twigs.permission;

import com.wbrawner.twigs.budget.Budget;
import com.wbrawner.twigs.user.User;

import javax.persistence.*;

@Entity
public class UserPermission {
    @EmbeddedId
    private UserPermissionKey id;
    @ManyToOne
    @MapsId("budgetId")
    @JoinColumn(nullable = false, name = "budget_id")
    private Budget budget;
    @ManyToOne
    @MapsId("userId")
    @JoinColumn(nullable = false, name = "user_id")
    private User user;
    @JoinColumn(nullable = false)
    @Enumerated(EnumType.STRING)
    private Permission permission;

    public UserPermission() {
        this(null, null, null, null);
    }

    public UserPermission(Budget budget, User user, Permission permission) {
        this(new UserPermissionKey(budget.getId(), user.getId()), budget, user, permission);
    }

    public UserPermission(UserPermissionKey userPermissionKey, Budget budget, User user, Permission permission) {
        this.id = userPermissionKey;
        this.budget = budget;
        this.user = user;
        this.permission = permission;
    }

    public UserPermissionKey getId() {
        return id;
    }

    public Budget getBudget() {
        return budget;
    }

    public User getUser() {
        return user;
    }

    public Permission getPermission() {
        return permission;
    }
}
