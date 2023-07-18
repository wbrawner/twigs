package com.wbrawner.twigs.transaction;

public class BalanceResponse {
    private final String id;
    private final long balance;

    public BalanceResponse(String id, long balance) {
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
