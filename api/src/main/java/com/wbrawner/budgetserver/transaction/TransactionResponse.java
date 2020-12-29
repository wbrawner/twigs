package com.wbrawner.budgetserver.transaction;

class TransactionResponse {
    private final String id;
    private final String title;
    private final String description;
    private final String date;
    private final Long amount;
    private final Boolean expense;
    private final String budgetId;
    private final String categoryId;
    private final String createdBy;

    TransactionResponse(String id,
                        String title,
                        String description,
                        String date,
                        Long amount,
                        Boolean expense,
                        String budgetId,
                        String categoryId,
                        String createdBy) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.amount = amount;
        this.expense = expense;
        this.budgetId = budgetId;
        this.categoryId = categoryId;
        this.createdBy = createdBy;
    }

    TransactionResponse(Transaction transaction) {
        this(
                transaction.getId(),
                transaction.getTitle(),
                transaction.getDescription(),
                transaction.getDate().toString(),
                transaction.getAmount(),
                transaction.getExpense(),
                transaction.getBudget().getId(),
                transaction.getCategory() != null ? transaction.getCategory().getId() : null,
                transaction.getCreatedBy().getId()
        );
    }

    public String getId() {
        return id;
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

    public Boolean getExpense() {
        return expense;
    }

    public String getBudgetId() {
        return budgetId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}