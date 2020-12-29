package com.wbrawner.budgetserver.transaction;

class NewTransactionRequest {
    private final String title;
    private final String description;
    private final String date;
    private final Long amount;
    private final String categoryId;
    private final Boolean expense;
    private final String budgetId;

    NewTransactionRequest() {
        this(null, null, null, null, null, null, null);
    }

    NewTransactionRequest(String title, String description, String date, Long amount, String categoryId, Boolean expense, String budgetId) {
        this.title = title;
        this.description = description;
        this.date = date;
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

    public String getDate() {
        return date;
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