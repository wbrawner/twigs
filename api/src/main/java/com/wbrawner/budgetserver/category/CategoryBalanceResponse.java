package com.wbrawner.budgetserver.category;

public class CategoryBalanceResponse {
    private final long id;
    private final long balance;

    public CategoryBalanceResponse(long id, long balance) {
        this.id = id;
        this.balance = balance;
    }

    public long getId() {
        return id;
    }

    public long getBalance() {
        return balance;
    }
}
