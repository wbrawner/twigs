package com.wbrawner.twigs.user;

public class UserResponse {
    private final String id;
    private final String username;
    private final String email;

    public UserResponse(User user) {
        this(user.getId(), user.getUsername(), user.getEmail());
    }

    public UserResponse(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
