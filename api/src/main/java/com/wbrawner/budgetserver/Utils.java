package com.wbrawner.budgetserver;

import com.wbrawner.budgetserver.user.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

public final class Utils {
    private static final int[] CALENDAR_FIELDS = new int[]{
            Calendar.MILLISECOND,
            Calendar.SECOND,
            Calendar.MINUTE,
            Calendar.HOUR_OF_DAY,
            Calendar.DATE
    };

    public static Date getFirstOfMonth() {
        GregorianCalendar calendar = new GregorianCalendar();
        for (int field : CALENDAR_FIELDS) {
            calendar.set(field, calendar.getActualMinimum(field));
        }
        return calendar.getTime();
    }

    public static Date getEndOfMonth() {
        GregorianCalendar calendar = new GregorianCalendar();
        for (int field : CALENDAR_FIELDS) {
            calendar.set(field, calendar.getActualMaximum(field));
        }
        return calendar.getTime();
    }

    public static User getCurrentUser() {
        Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user instanceof User) {
            return (User) user;
        }

        return null;
    }

    public static String randomId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
