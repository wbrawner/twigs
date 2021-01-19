package com.wbrawner.budgetserver.session;

import java.util.Date;

public class SessionResponse {
    private final String token;
    private final String expiration;

    public SessionResponse(Session session) {
        this(session.getToken(), session.getExpiration());
    }

    public SessionResponse(String token, Date expiration) {
        this.token = token;
        this.expiration = expiration.toInstant().toString();
    }

    public String getToken() {
        return token;
    }

    public String getExpiration() {
        return expiration;
    }
}
