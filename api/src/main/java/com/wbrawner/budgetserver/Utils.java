package com.wbrawner.budgetserver;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public final class Utils {
    private static int[] CALENDAR_FIELDS = new int[]{
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
}
