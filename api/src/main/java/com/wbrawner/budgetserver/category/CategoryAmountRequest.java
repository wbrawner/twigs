package com.wbrawner.budgetserver.category;

import java.util.Calendar;

public class CategoryAmountRequest {
    private final Long amount;
    private final Integer month;
    private final Integer year;

    public CategoryAmountRequest() {
        this(null, null, null);
    }

    public CategoryAmountRequest(Long amount, Integer month, Integer year) {
        this.amount = amount;
        this.month = month;
        this.year = year;
    }

    public long getAmount() {
        return amount != null ? amount : 0L;
    }

    public int getMonth() {
        return month != null ? month : Calendar.getInstance().get(Calendar.MONTH);
    }

    public int getYear() {
        return year != null ? year : Calendar.getInstance().get(Calendar.YEAR);
    }
}
