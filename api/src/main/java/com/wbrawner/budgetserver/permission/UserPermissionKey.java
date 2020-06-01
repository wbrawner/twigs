package com.wbrawner.budgetserver.permission;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class UserPermissionKey implements Serializable {
    private final Long budgetId;
    private final Long userId;

    public UserPermissionKey() {
        this(0, 0);
    }

    public UserPermissionKey(long budgetId, long userId) {
        this.budgetId = budgetId;
        this.userId = userId;
    }
}
