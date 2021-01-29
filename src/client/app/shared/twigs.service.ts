import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';
import { User, UserPermission } from '../users/user';
import { Budget } from '../budgets/budget';
import { Category } from '../categories/category';
import { Transaction } from '../transactions/transaction';

export interface TwigsService {
  // Auth
  login(email: string, password: string): Observable<User>;
  register(username: string, email: string, password: string): Observable<User>;
  logout(): Observable<void>;

  // Budgets
  getBudgets(): Observable<Budget[]>;
  getBudget(id: string): Observable<Budget>;
  getBudgetBalance(id: string): Observable<number>;
  createBudget(
    id: string,
    name: string,
    description: string,
    users: UserPermission[],
  ): Observable<Budget>;
  updateBudget(id: string, changes: object): Observable<Budget>;
  deleteBudget(id: string): Observable<void>;

  // Categories
  getCategories(budgetId?: string, count?: number): Observable<Category[]>;
  getCategory(id: string): Observable<Category>;
  getCategoryBalance(id: string): Observable<number>;
  createCategory(id: string, budgetId: string, name: string, description: string, amount: number, isExpense: boolean): Observable<Category>;
  updateCategory(id: string, changes: object): Observable<Category>;
  deleteCategory(id: string): Observable<void>;

  // Transactions
  getTransactions(
    budgetId?: string,
    categoryId?: string,
    count?: number,
    from?: Date,
    to?: Date
  ): Observable<Transaction[]>;
  getTransaction(id: string): Observable<Transaction>;
  createTransaction(
    id: string,
    budgetId: string,
    name: string,
    description: string,
    amount: number,
    date: Date,
    isExpense: boolean,
    category: string
  ): Observable<Transaction>;
  updateTransaction(id: string, changes: object): Observable<Transaction>;
  deleteTransaction(id: string): Observable<void>;

  getProfile(): Observable<User>;
  getUsersByUsername(username: string): Observable<User[]>;
}

export let TWIGS_SERVICE = new InjectionToken<TwigsService>('twigs.service');
