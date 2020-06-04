package com.wbrawner.budgetserver.category;

import com.wbrawner.budgetserver.budget.Budget;

import javax.persistence.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Category implements Comparable<Category> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private final Long id = null;
    private String title;
    private String description;
    @JoinColumn(nullable = false)
    @OneToMany(mappedBy = "category", fetch = FetchType.EAGER)
    private final Set<CategoryAmount> amounts = new HashSet<>();
    @JoinColumn(nullable = false)
    @ManyToOne
    private Budget budget;
    private boolean expense;

    public Category() {
        this(null, null, null, true);
    }

    public Category(String title, String description, Budget budget, boolean expense) {
        this.title = title;
        this.description = description;
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

    /**
     * Returns the effective amount for the given year and month.
     *
     * Category amounts are stored via the {@link CategoryAmount} object, where the month is the first month where
     * the given amount is active and the year is the first year the given amount is active. So if you created a
     * category in January of 2020 and then queried the amount for April of 2020, the amount would be returned from
     * January. If in May the amount is updated, then that amount would be returned for all the months following May
     * until the next update.
     * @param month the maximum month to search
     * @param year the maximum year to search
     * @return the amount value from the {@link CategoryAmount} that corresponds to the given month and year, or 0 if
     * no current or previous amounts can be found.
     */
    public long getAmount(int month, int year) {
        return this.amounts.stream()
                .filter(categoryAmount -> categoryAmount.getMonth() <= month && categoryAmount.getYear() <= year)
                .max(Comparator.comparingInt(CategoryAmount::getYear).thenComparingInt(CategoryAmount::getMonth))
                .orElse(new CategoryAmount(this, 0L, month, year))
                .getAmount();
    }

    public long getMostRecentAmount() {
        return this.amounts.stream()
                .sorted()
                .findFirst()
                .orElse(new CategoryAmount(this, 0L, 0, 0))
                .getAmount();
    }

    public void setAmount(long amount, int month, int year) {
        CategoryAmount categoryAmount = this.amounts.stream()
                .filter(ca -> ca.getMonth() == month && ca.getYear() == year)
                .findAny()
                .orElse(null);
        if (categoryAmount == null) {
            categoryAmount = new CategoryAmount();
            this.amounts.add(categoryAmount);
        }
        categoryAmount.setAmount(amount);
        categoryAmount.setMonth(month);
        categoryAmount.setYear(year);
    }

    public void setAmount(CategoryAmountRequest amount) {
        setAmount(amount.getAmount(), amount.getMonth(), amount.getYear());
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
