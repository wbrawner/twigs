package com.wbrawner.budgetserver.passwordresetrequest;

import com.wbrawner.budgetserver.user.User;

import javax.persistence.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

import static com.wbrawner.budgetserver.Utils.randomId;

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