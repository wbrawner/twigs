package com.wbrawner.budgetserver.category;

public class UpdateCategoryRequest {
    private final String title;
    private final String description;
    private final CategoryAmountRequest amount;
    private final Boolean expense;

    public UpdateCategoryRequest() {
        this(null, null, null, null);
    }

    public UpdateCategoryRequest(
            String title,
            String description,
            CategoryAmountRequest amount,
            Boolean expense
    ) {
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.expense = expense;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public CategoryAmountRequest getAmount() {
        return amount;
    }

    public Boolean getExpense() {
        return expense;
    }
}
