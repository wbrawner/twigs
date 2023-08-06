package com.wbrawner.twigs;

import com.wbrawner.twigs.user.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

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

    public static Date twoWeeksFromNow() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DATE, 14);
        return calendar.getTime();
    }

    public static User getCurrentUser() {
        Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user instanceof User) {
            return (User) user;
        }

        return null;
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random random = new SecureRandom();

    public static String randomString(int length) {
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < length; i++) {
            id.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return id.toString();
    }

    public static String randomId() {
        return randomString(32);
    }
}
