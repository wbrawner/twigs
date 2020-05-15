package com.wbrawner.budgetserver.budget;

import com.wbrawner.budgetserver.category.Category;
import com.wbrawner.budgetserver.transaction.Transaction;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

@Entity
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String currencyCode;
    @OneToMany(mappedBy = "budget")
    private final Set<Transaction> transactions = new TreeSet<>();
    @OneToMany(mappedBy = "budget")
    private final Set<Category> categories = new TreeSet<>();
    @OneToMany(mappedBy = "budget")
    private final Set<Transaction> users = new HashSet<>();

    public Budget() {}

    public Budget(String name, String description) {
        this(name, description, "USD");
    }

    public Budget(String name, String description, String currencyCode) {
        this.name = name;
        this.description = description;
        this.currencyCode = currencyCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Set<Transaction> getTransactions() {
        return transactions;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public Set<Transaction> getUsers() {
        return users;
    }
}
