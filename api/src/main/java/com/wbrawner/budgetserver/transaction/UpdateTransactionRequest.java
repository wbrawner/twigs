package com.wbrawner.budgetserver.transaction;

class UpdateTransactionRequest {
    private final String title;
    private final String description;
    private final String date;
    private final Long amount;
    private final Long categoryId;
    private final Boolean expense;
    private final Long budgetId;
    private final Long createdBy;

    UpdateTransactionRequest() {
        this(null, null, null, null, null, null, null, null);
    }

    UpdateTransactionRequest(String title, String description, String date, Long amount, Long categoryId, Boolean expense, Long budgetId, Long createdBy) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.amount = amount;
        this.categoryId = categoryId;
        this.expense = expense;
        this.budgetId = budgetId;
        this.createdBy = createdBy;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public Long getAmount() {
        return amount;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Boolean getExpense() {
        return expense;
    }

    public Long getBudgetId() {
        return budgetId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }
}