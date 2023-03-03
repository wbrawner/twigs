package com.wbrawner.twigs.category;

import java.util.Objects;

public class CategoryResponse {
    private final String id;
    private final String title;
    private final String description;
    private final long amount;
    private final String budgetId;
    private final boolean expense;
    private final boolean archived;

    public CategoryResponse(Category category) {
        this(
                Objects.requireNonNull(category.getId()),
                category.getTitle(),
                category.getDescription(),
                category.getAmount(),
                Objects.requireNonNull(category.getBudget()).getId(),
                category.isExpense(),
                category.isArchived()
        );
    }

    public CategoryResponse(
            String id, String title, String description, long amount, String budgetId, boolean expense, boolean archived
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.budgetId = budgetId;
        this.expense = expense;
        this.archived = archived;
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

    public long getAmount() {
        return amount;
    }

    public String getBudgetId() {
        return budgetId;
    }

    public boolean isExpense() {
        return expense;
    }

    public boolean isArchived() {
        return archived;
    }
}
