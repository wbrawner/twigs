package com.wbrawner.twigs.session;

import java.util.Date;

public class SessionResponse {
    private final String userId;

    private final String token;
    private final String expiration;

    public SessionResponse(Session session) {
        this(session.getUserId(), session.getToken(), session.getExpiration());
    }

    public SessionResponse(String userId, String token, Date expiration) {
        this.userId = userId;
        this.token = token;
        this.expiration = expiration.toInstant().toString();
    }

    public String getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public String getExpiration() {
        return expiration;
    }
}
