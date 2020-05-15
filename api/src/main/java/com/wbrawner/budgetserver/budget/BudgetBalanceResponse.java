package com.wbrawner.budgetserver.budget;

public class BudgetBalanceResponse {
    public final long id;
    public final long balance;

    public BudgetBalanceResponse(long id, long balance) {
        this.id = id;
        this.balance = balance;
    }
}
