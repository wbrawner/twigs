package com.wbrawner.budgetserver.category;

import com.wbrawner.budgetserver.budget.Budget;

import javax.persistence.*;

@Entity
public class Category implements Comparable<Category> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private final Long id = null;
    private String title;
    private String description;
    private long amount;
    @JoinColumn(nullable = false)
    @ManyToOne
    private Budget budget;
    private boolean expense;

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

    public Long getId() {
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
}
