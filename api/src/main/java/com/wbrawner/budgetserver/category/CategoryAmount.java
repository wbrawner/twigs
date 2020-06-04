package com.wbrawner.budgetserver.category;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Comparator;

@Entity
public class CategoryAmount implements Comparable<CategoryAmount> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private Category category;
    private long amount;
    private int month;
    private int year;

    public CategoryAmount(Category category, long amount, int month, int year) {
        this.category = category;
        this.amount = amount;
        if (month < 0 || month > 11) throw new IllegalArgumentException("Invalid month");
        this.month = month;
        this.year = year;
    }

    public CategoryAmount() {
        // required empty constructor for Hibernate
        this(null, 0L, 0, 0);
    }

    public Long getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public long getAmount() {
        return amount;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public int compareTo(@NotNull CategoryAmount o) {
        int yearComparison = Integer.compare(o.getYear(), getYear());
        if (yearComparison != 0) return yearComparison;
        return Integer.compare(o.getMonth(), getMonth());
    }
}
