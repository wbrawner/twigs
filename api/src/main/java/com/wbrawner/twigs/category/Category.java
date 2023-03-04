package com.wbrawner.twigs.category;

import com.wbrawner.twigs.budget.Budget;
import jakarta.persistence.*;

import static com.wbrawner.twigs.Utils.randomId;

@Entity
public class Category implements Comparable<Category> {
    @Id
    private final String id = randomId();
    private String title;
    private String description;
    private long amount;
    @JoinColumn(nullable = false)
    @ManyToOne
    private Budget budget;
    private boolean expense;
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean archived;

    public Category() {
        this(null, null, 0L, null, true);
    }

    public Category(
            String title,
            String description,
            Long amount,
            Budget budget,
            boolean expense
    ) {
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.budget = budget;
        this.expense = expense;
    }

    @Override
    public int compareTo(Category other) {
        return title.compareTo(other.title);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    public boolean isExpense() {
        return expense;
    }

    public void setExpense(boolean expense) {
        this.expense = expense;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}
