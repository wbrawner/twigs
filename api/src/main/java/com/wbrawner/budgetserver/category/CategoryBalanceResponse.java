package com.wbrawner.budgetserver.category;

public class CategoryBalanceResponse {
    private final String id;
    private final long balance;

    public CategoryBalanceResponse(String id, long balance) {
        this.id = id;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public long getBalance() {
        return balance;
    }
}
