package com.wbrawner.twigs.budget;

import com.wbrawner.twigs.category.Category;
import com.wbrawner.twigs.transaction.Transaction;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static com.wbrawner.twigs.Utils.randomId;

@Entity
public class Budget {
    @Id
    private String id = randomId();
    private String name;
    private String description;
    private String currencyCode;
    @OneToMany(mappedBy = "budget")
    private final Set<Transaction> transactions = new TreeSet<>();
    @OneToMany(mappedBy = "budget")
    private final Set<Category> categories = new TreeSet<>();
    @OneToMany(mappedBy = "budget")
    private final Set<Transaction> users = new HashSet<>();

    public Budget() {
    }

    public Budget(String name, String description) {
        this(name, description, "USD");
    }

    public Budget(String name, String description, String currencyCode) {
        this.name = name;
        this.description = description;
        this.currencyCode = currencyCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
