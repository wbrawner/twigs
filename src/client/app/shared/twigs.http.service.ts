import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, pipe, Subscriber } from 'rxjs';
import { User, UserPermission, Permission, AuthToken } from '../users/user';
import { TwigsService } from './twigs.service';
import { Budget } from '../budgets/budget';
import { Category } from '../categories/category';
import { Transaction } from '../transactions/transaction';
import { environment } from '../../environments/environment';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class TwigsHttpService implements TwigsService {

  private options = {
    withCredentials: true
  };
  private apiUrl = environment.apiUrl;
  private budgets: BehaviorSubject<Budget[]> = new BehaviorSubject(null);

  constructor(
    private http: HttpClient,
    private storage: Storage
  ) { }

  login(email: string, password: string): Observable<User> {
    return new Observable(emitter => {
      const params = {
        'username': email,
        'password': password
      };
      this.http.post<AuthToken>(this.apiUrl + '/users/login', params, this.options)
        .subscribe(
          auth => {
            // TODO: Use token expiration to determine cookie expiration
            this.storage.setItem('Authorization', auth.token);
            this.getProfile().subscribe(user => emitter.next(user), error => emitter.error(error));
          },
          error => emitter.error(error)
        );
    });
  }

  register(username: string, email: string, password: string): Observable<User> {
    const params = {
      'username': username,
      'email': email,
      'password': password
    };
    return this.http.post<User>(this.apiUrl + '/users', params, this.options);
  }

  logout(): Observable<void> {
    return new Observable(emitter => {
      this.storage.removeItem('Authorization');
      emitter.next();
      emitter.complete();
    })
    // TODO: Implement this when JWT auth is implemented
    // return this.http.post<void>(this.apiUrl + '/login?logout', this.options);
  }

  // Budgets
  getBudgets(): Observable<Budget[]> {
    this.http.get<Budget[]>(this.apiUrl + '/budgets', this.options)
      .subscribe(budgets => {
        this.budgets.next(budgets);
      });
    return this.budgets;
  }

  getBudgetBalance(id: string): Observable<number> {
    return this.http.get<any>(`${this.apiUrl}/budgets/${id}/balance`, this.options)
      .pipe(map(obj => obj.balance));
  }

  getBudget(id: string): Observable<Budget> {
    return new Observable(emitter => {
      var cachedBudget: Budget
      if (this.budgets.value) {
        cachedBudget = this.budgets.value.find(budget => {
          return budget.id === id;
        });
      }
      if (cachedBudget) {
        emitter.next(cachedBudget);
        emitter.complete();
      } else {
        this.http.get<Budget>(`${this.apiUrl}/budgets/${id}`, this.options)
          .subscribe(budget => {
            var oldBudgets = JSON.parse(JSON.stringify(this.budgets.value));
            if (!oldBudgets) {
              oldBudgets = [];
            }
            oldBudgets.push(budget);
            oldBudgets.sort();
            this.budgets.next(oldBudgets);
            emitter.next(budget);
            emitter.complete();
          })
      }
    })
  }

  createBudget(
    id: string,
    name: string,
    description: string,
    users: UserPermission[],
  ): Observable<Budget> {
    const params = {
      'id': id,
      'name': name,
      'description': description,
      'users': users.map(userPermission => {
        return {
          user: userPermission.user.id,
          permission: Permission[userPermission.permission]
        };
      })
    };
    return this.http.post<Budget>(this.apiUrl + '/budgets', params, this.options)
      .pipe(map(budget => {
        var updatedBudgets = JSON.parse(JSON.stringify(this.budgets.value));
        updatedBudgets.push(budget);
        updatedBudgets.sort();
        this.budgets.next(updatedBudgets);
        return budget
      }))
  }

  updateBudget(id: string, changes: object): Observable<Budget> {
    let budget = changes as Budget;
    const params = {
      'name': budget.name,
      'description': budget.description,
      'users': budget.users.map(userPermission => {
        return {
          user: userPermission.user.id,
          permission: Permission[userPermission.permission]
        };
      })
    };
    return this.http.put<Budget>(`${this.apiUrl}/budgets/${id}`, params, this.options)
      .pipe(map(budget => {
        var updatedBudgets: Budget[] = JSON.parse(JSON.stringify(this.budgets.value));
        var index = updatedBudgets.findIndex(oldBudget => oldBudget.id === id);
        if (index > -1) {
          updatedBudgets.splice(index, 1);
        }
        updatedBudgets.push(budget);
        updatedBudgets.sort();
        this.budgets.next(updatedBudgets);
        return budget
      }));
  }

  deleteBudget(id: String): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/budgets/${id}`, this.options)
      .pipe(map(() => {
        var updatedBudgets: Budget[] = JSON.parse(JSON.stringify(this.budgets.value));
        var index = updatedBudgets.findIndex(oldBudget => oldBudget.id === id);
        if (index > -1) {
          updatedBudgets.splice(index, 1);
        }
        updatedBudgets.sort();
        this.budgets.next(updatedBudgets);
        return;
      }));
  }

  // Categories
  getCategories(budgetId: string, count?: number): Observable<Category[]> {
    const params = {
      params: new HttpParams()
        .set('budgetIds', `${budgetId}`)
    };
    return this.http.get<Category[]>(`${this.apiUrl}/categories`, Object.assign(params, this.options));
  }

  getCategory(id: string): Observable<Category> {
    return this.http.get<Category>(`${this.apiUrl}/categories/${id}`, this.options);
  }

  getCategoryBalance(id: string): Observable<number> {
    return this.http.get<any>(`${this.apiUrl}/categories/${id}/balance`, this.options)
      .pipe(map(obj => obj.balance));
  }

  createCategory(id: string, budgetId: string, name: string, description: string, amount: number, isExpense: boolean): Observable<Category> {
    const params = {
      'id': id,
      'title': name,
      'description': description,
      'amount': amount,
      'expense': isExpense,
      'budgetId': budgetId
    };
    return this.http.post<Category>(this.apiUrl + '/categories', params, this.options);
  }

  updateCategory(id: string, changes: object): Observable<Category> {
    return this.http.put<Category>(`${this.apiUrl}/categories/${id}`, changes, this.options);
  }

  deleteCategory(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/categories/${id}`, this.options);
  }

  // Transactions
  getTransactions(
    budgetId?: string,
    categoryId?: string,
    count?: number,
    from?: Date,
    to?: Date
  ): Observable<Transaction[]> {
    let httpParams = new HttpParams();
    if (budgetId) {
      httpParams = httpParams.set('budgetIds', `${budgetId}`);
    }
    if (categoryId) {
      httpParams = httpParams.set('categoryIds', `${categoryId}`);
    }
    if (from) {
      httpParams = httpParams.set('from', from.toISOString());
    }
    if (to) {
      httpParams = httpParams.set('to', to.toISOString());
    }
    const params = { params: httpParams };
    return this.http.get<Transaction[]>(`${this.apiUrl}/transactions`, Object.assign(params, this.options))
      .pipe(map(transactions => {
        transactions.forEach(transaction => {
          transaction.date = new Date(transaction.date);
        });
        return transactions;
      }));
  }

  getTransaction(id: string): Observable<Transaction> {
    return this.http.get<Transaction>(`${this.apiUrl}/transactions/${id}`, this.options)
      .pipe(map(transaction => {
        transaction.date = new Date(transaction.date);
        return transaction;
      }));
  }

  createTransaction(
    id: string,
    budgetId: string,
    name: string,
    description: string,
    amount: number,
    date: Date,
    expense: boolean,
    category: string
  ): Observable<Transaction> {
    const params = {
      'id': id,
      'title': name,
      'description': description,
      'date': date.toISOString(),
      'amount': amount,
      'expense': expense,
      'categoryId': category,
      'budgetId': budgetId
    };
    return this.http.post<Transaction>(this.apiUrl + '/transactions', params, this.options);
  }

  updateTransaction(id: string, changes: object): Observable<Transaction> {
    return this.http.put<Transaction>(`${this.apiUrl}/transactions/${id}`, changes, this.options);
  }

  deleteTransaction(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/transactions/${id}`, this.options);
  }

  // Users
  getProfile(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/users/me`, this.options);
  }

  getUsersByUsername(username: string): Observable<User[]> {
    return new Observable(subscriber => {
      subscriber.error("Not yet implemented")
    });
  }
}
