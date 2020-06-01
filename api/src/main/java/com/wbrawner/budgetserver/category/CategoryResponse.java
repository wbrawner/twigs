package com.wbrawner.budgetserver.category;

import java.util.Objects;

public class CategoryResponse {
    private final long id;
    private final String title;
    private final String description;
    private final long amount;
    private final long budgetId;
    private final boolean expense;

    public CategoryResponse(Category category) {
        this(
                Objects.requireNonNull(category.getId()),
                category.getTitle(),
                category.getDescription(),
                category.getAmount(),
                Objects.requireNonNull(category.getBudget()).getId(),
                category.isExpense()
        );
    }

    public CategoryResponse(long id, String title, String description, long amount, long budgetId, boolean expense) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.budgetId = budgetId;
        this.expense = expense;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getAmount() {
        return amount;
    }

    public long getBudgetId() {
        return budgetId;
    }

    public boolean isExpense() {
        return expense;
    }
}
