package com.wbrawner.budgetserver.transaction;

class TransactionResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final String date;
    private final Long amount;
    private final Boolean expense;
    private final Long budgetId;
    private final Long categoryId;
    private final Long createdBy;

    TransactionResponse(Long id,
                        String title,
                        String description,
                        String date,
                        Long amount,
                        Boolean expense,
                        Long budgetId,
                        Long categoryId,
                        Long createdBy) {
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

    public Long getId() {
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

    public Long getBudgetId() {
        return budgetId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }
}