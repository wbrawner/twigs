package com.wbrawner.twigs.permission;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class UserPermissionKey implements Serializable {
    private final String budgetId;
    private final String userId;

    public UserPermissionKey() {
        this(null, null);
    }

    public UserPermissionKey(String budgetId, String userId) {
        this.budgetId = budgetId;
        this.userId = userId;
    }
}
