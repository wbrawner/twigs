import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TransactionsComponent } from './transactions/transactions.component';
import { TransactionDetailsComponent } from './transactions/transaction-details/transaction-details.component';
import { NewTransactionComponent } from './transactions/new-transaction/new-transaction.component';
import { CategoriesComponent } from './categories/categories.component';
import { CategoryDetailsComponent } from './categories/category-details/category-details.component';
import { NewCategoryComponent } from './categories/new-category/new-category.component';
import { LoginComponent } from './users/login/login.component';
import { RegisterComponent } from './users/register/register.component';
import { BudgetsComponent } from './budgets/budget.component';
import { NewBudgetComponent } from './budgets/new-budget/new-budget.component';
import { EditBudgetComponent } from './budgets/edit-budget/edit-budget.component';
import { BudgetDetailsComponent } from './budgets/budget-details/budget-details.component';
import { EditCategoryComponent } from './categories/edit-category/edit-category.component';

const routes: Routes = [
  { path: '', component: BudgetsComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'budgets', component: BudgetsComponent },
  { path: 'budgets/new', component: NewBudgetComponent },
  { path: 'budgets/:id', component: BudgetDetailsComponent },
  { path: 'budgets/:id/edit', component: EditBudgetComponent },
  { path: 'transactions', component: TransactionsComponent },
  { path: 'transactions/new', component: NewTransactionComponent },
  { path: 'transactions/:id', component: TransactionDetailsComponent },
  { path: 'categories', component: CategoriesComponent },
  { path: 'categories/new', component: NewCategoryComponent },
  { path: 'categories/:id', component: CategoryDetailsComponent },
  { path: 'categories/:id/edit', component: EditCategoryComponent },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, { relativeLinkResolution: 'legacy' })
  ],
  exports: [
    RouterModule
  ],
  declarations: []
})
export class AppRoutingModule { }
