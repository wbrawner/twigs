package com.wbrawner.budgetserver.recurrence;

class RecurringTransactionResponse {
    public final String id;
    public final String title;
    public final String description;
    public final long amount;
    public final boolean expense;
    public final String budgetId;
    public final String categoryId;
    public final String createdBy;
    public final String timeZone;
    public final int time;
    public final RecurringTransaction.FrequencyUnit frequencyUnit;
    public final int frequencyValue;


    RecurringTransactionResponse(
            String id,
            String title,
            String description,
            long amount,
            boolean expense,
            String budgetId,
            String categoryId,
            String createdBy,
            String timeZone,
            int time,
            RecurringTransaction.FrequencyUnit frequencyUnit,
            int frequencyValue
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.expense = expense;
        this.budgetId = budgetId;
        this.categoryId = categoryId;
        this.createdBy = createdBy;
        this.timeZone = timeZone;
        this.time = time;
        this.frequencyUnit = frequencyUnit;
        this.frequencyValue = frequencyValue;
    }

    RecurringTransactionResponse(RecurringTransaction recurringTransaction) {
        this(
                recurringTransaction.getId(),
                recurringTransaction.getTitle(),
                recurringTransaction.getDescription(),
                recurringTransaction.getAmount(),
                recurringTransaction.isExpense(),
                recurringTransaction.getBudget().getId(),
                recurringTransaction.getCategory() != null ? recurringTransaction.getCategory().getId() : null,
                recurringTransaction.getCreatedBy().getId(),
                recurringTransaction.getTimeZone(),
                recurringTransaction.getTimeOfDayInSeconds(),
                recurringTransaction.getFrequencyUnit(),
                recurringTransaction.getFrequencyValue()
        );
    }
}