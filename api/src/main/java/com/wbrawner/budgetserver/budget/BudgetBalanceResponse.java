package com.wbrawner.budgetserver.budget;

public class BudgetBalanceResponse {
    public final String id;
    public final long balance;

    public BudgetBalanceResponse(String id, long balance) {
        this.id = id;
        this.balance = balance;
    }
}
