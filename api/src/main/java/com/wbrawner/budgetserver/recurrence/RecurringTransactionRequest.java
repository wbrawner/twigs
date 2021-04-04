package com.wbrawner.budgetserver.recurrence;

class RecurringTransactionRequest {
    private final String title;
    private final String description;
    private final Long amount;
    private final String categoryId;
    private final Boolean expense;
    private final String budgetId;
    private final String timeZone;
    private final Integer time;
    private final RecurringTransaction.FrequencyUnit frequencyUnit;
    private final Integer frequencyValue;


    RecurringTransactionRequest() {
        this(null, null, null, 0, null, 0, null, null, null, null);
    }

    RecurringTransactionRequest(
            String title,
            String description,
            RecurringTransaction.FrequencyUnit frequencyUnit,
            int frequencyValue,
            String timeZone,
            int time,
            Long amount,
            String categoryId,
            Boolean expense,
            String budgetId
    ) {
        this.title = title;
        this.description = description;
        this.frequencyUnit = frequencyUnit;
        this.frequencyValue = frequencyValue;
        this.timeZone = timeZone;
        this.time = time;
        this.amount = amount;
        this.categoryId = categoryId;
        this.expense = expense;
        this.budgetId = budgetId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public Integer getTime() {
        return time;
    }

    public RecurringTransaction.FrequencyUnit getFrequencyUnit() {
        return frequencyUnit;
    }

    public Integer getFrequencyValue() {
        return frequencyValue;
    }

    public Long getAmount() {
        return amount;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public Boolean getExpense() {
        return expense;
    }

    public String getBudgetId() {
        return budgetId;
    }
}