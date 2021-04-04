package com.wbrawner.budgetserver.recurrence;

import com.wbrawner.budgetserver.budget.Budget;
import com.wbrawner.budgetserver.category.Category;
import com.wbrawner.budgetserver.user.User;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.TimeZone;

import static com.wbrawner.budgetserver.Utils.randomId;

@Entity
public class RecurringTransaction {
    @Id
    private final String id = randomId();
    @ManyToOne
    @JoinColumn(nullable = false)
    private final User createdBy;
    private String title;
    private String description;
    private Long amount;
    @ManyToOne
    private Category category;
    private Boolean expense;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Budget budget;
    private String timeZone;
    private int time;
    private FrequencyUnit frequencyUnit;
    private int frequencyValue;

    public RecurringTransaction() {
        this(
                null,
                null,
                FrequencyUnit.DAILY,
                0,
                null,
                0,
                null,
                null,
                null,
                null,
                null
        );
    }

    public RecurringTransaction(
            String title,
            String description,
            FrequencyUnit frequencyUnit,
            int frequencyValue,
            String timeZone,
            int time,
            Long amount,
            Category category,
            Boolean expense,
            User createdBy,
            Budget budget
    ) {
        this.title = title;
        this.description = description;
        this.frequencyUnit = frequencyUnit;
        this.frequencyValue = frequencyValue;
        setTimeZone(timeZone);
        this.time = time;
        this.amount = amount;
        this.category = category;
        this.expense = expense;
        this.createdBy = createdBy;
        this.budget = budget;
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

    public FrequencyUnit getFrequencyUnit() {
        return frequencyUnit;
    }

    public int getFrequencyValue() {
        return frequencyValue;
    }

    public void setFrequency(FrequencyUnit frequencyUnit, int frequencyValue) {
        if (frequencyValue < 0) throw new IllegalArgumentException("frequencyValue must be at least 0");
        if (frequencyValue > frequencyUnit.maxValue) {
            throw new IllegalArgumentException(String.format(
                    "Invalid frequencyValue. Requested %d for %s but maxValue is %d",
                    frequencyValue,
                    frequencyUnit.name(),
                    frequencyUnit.maxValue
            ));
        }
        this.frequencyUnit = frequencyUnit;
        this.frequencyValue = frequencyValue;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = TimeZone.getTimeZone(timeZone).getID();
    }

    public int getTimeOfDayInSeconds() {
        return time;
    }

    public void setTimeOfDayInSeconds(int time) {
        this.time = time;
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

    public Boolean isExpense() {
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

    enum FrequencyUnit {
        DAILY(0),
        WEEKLY(7),
        MONTHLY(30),
        YEARLY(365);

        int maxValue;

        FrequencyUnit(int maxValue) {
            this.maxValue = maxValue;
        }
    }
}