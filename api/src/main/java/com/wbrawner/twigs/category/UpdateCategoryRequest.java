package com.wbrawner.twigs.category;

public class UpdateCategoryRequest {
    private final String title;
    private final String description;
    private final Long amount;
    private final Boolean expense;
    private final Boolean archived;

    public UpdateCategoryRequest() {
        this(null, null, null, null, null);
    }

    public UpdateCategoryRequest(String title, String description, Long amount, Boolean expense, Boolean archived) {
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.expense = expense;
        this.archived = archived;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Long getAmount() {
        return amount;
    }

    public Boolean getExpense() {
        return expense;
    }

    public Boolean getArchived() {
        return archived;
    }
}
