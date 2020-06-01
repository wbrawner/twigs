package com.wbrawner.budgetserver.passwordresetrequest;

import com.wbrawner.budgetserver.user.User;

import javax.persistence.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

@Entity
public class PasswordResetRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private final Long id;
    @ManyToOne
    private final User user;
    private final Calendar date;
    private final String token;

    public PasswordResetRequest() {
        this(null, null);
    }

    public PasswordResetRequest(Long id, User user) {
        this(id, user, new GregorianCalendar(), UUID.randomUUID().toString().replace("-", ""));
    }

    public PasswordResetRequest(
            Long id,
            User user,
            Calendar date,
            String token
    ) {
        this.id = id;
        this.user = user;
        this.date = date;
        this.token = token;
    }
}