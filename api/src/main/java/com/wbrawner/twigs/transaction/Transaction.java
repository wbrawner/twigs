package com.wbrawner.twigs.transaction;

import com.wbrawner.twigs.budget.Budget;
import com.wbrawner.twigs.category.Category;
import com.wbrawner.twigs.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.Instant;

import static com.wbrawner.twigs.Utils.randomId;

@Entity
public class Transaction implements Comparable<Transaction> {
    @Id
    private final String id = randomId();
    @ManyToOne
    @JoinColumn(nullable = false)
    private final User createdBy;
    private String title;
    private String description;
    private Instant date;
    private Long amount;
    @ManyToOne
    private Category category;
    private Boolean expense;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Budget budget;

    public Transaction() {
        this(null, null, null, null, null, null, null, null);
    }

    public Transaction(
            String title,
            String description,
            Instant date,
            Long amount,
            Category category,
            Boolean expense,
            User createdBy,
            Budget budget
    ) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.amount = amount;
        this.category = category;
        this.expense = expense;
        this.createdBy = createdBy;
        this.budget = budget;
    }

    public String getId() {
        // This should only be set from Hibernate so it shouldn't actually be null ever
        //noinspection ConstantConditions
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

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Boolean getExpense() {
        return expense;
    }

    public void setExpense(Boolean expense) {
        this.expense = expense;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    @Override
    public int compareTo(Transaction other) {
        return this.date.compareTo(other.date);
    }
}