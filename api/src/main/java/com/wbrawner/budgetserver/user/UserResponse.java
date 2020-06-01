package com.wbrawner.budgetserver.user;

public class UserResponse {
    private final long id;
    private final String username;
    private final String email;

    public UserResponse(User user) {
        this(user.getId(), user.getUsername(), user.getEmail());
    }

    public UserResponse(long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
