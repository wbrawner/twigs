package com.wbrawner.twigs.user;

import com.wbrawner.twigs.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.wbrawner.twigs.Utils.randomId;

@Entity
public class PasswordResetRequest {
    @Id
    private final String id = randomId();
    @ManyToOne
    private final User user;
    private final Calendar date;
    private final String token;

    public PasswordResetRequest() {
        this(null);
    }

    public PasswordResetRequest(User user) {
        this(user, new GregorianCalendar(), randomId());
    }

    public PasswordResetRequest(
            User user,
            Calendar date,
            String token
    ) {
        this.user = user;
        this.date = date;
        this.token = token;
    }
}