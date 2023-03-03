package com.wbrawner.twigs.user;

public class NewUserRequest {
    private final String username;
    private final String password;
    private final String email;

    public NewUserRequest() {
        this(null, null, null);
    }

    public NewUserRequest(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
}