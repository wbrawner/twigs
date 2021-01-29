import { Component, OnInit, Inject, OnDestroy } from '@angular/core';
import { Budget } from '../budget';
import { ActivatedRoute, Router } from '@angular/router';
import { AppComponent } from 'src/client/app/app.component';
import { Transaction } from 'src/client/app/transactions/transaction';
import { Category } from 'src/client/app/categories/category';
import { Observable } from 'rxjs';
import { Label } from 'ng2-charts';
import { ChartDataSets } from 'chart.js';
import { TWIGS_SERVICE, TwigsService } from 'src/client/app/shared/twigs.service';
import { Actionable } from '../../shared/actionable';

@Component({
  selector: 'app-budget-details',
  templateUrl: './budget-details.component.html',
  styleUrls: ['./budget-details.component.css']
})
export class BudgetDetailsComponent implements OnInit, OnDestroy, Actionable {

  budget: Budget;
  public budgetBalance: number;
  public transactions: Transaction[];
  public expenses: Category[] = [];
  public income: Category[] = [];
  categoryBalances: Map<string, number>;
  expectedIncome = 0;
  actualIncome = 0;
  expectedExpenses = 0;
  actualExpenses = 0;
  barChartLabels: Label[] = ['Income', 'Expenses'];
  barChartData: ChartDataSets[] = [
    { data: [0, 0], label: 'Expected' },
    { data: [0, 0], label: 'Actual' },
  ];

  constructor(
    private app: AppComponent,
    private route: ActivatedRoute,
    @Inject(TWIGS_SERVICE) private twigsService: TwigsService,
    private router: Router,
  ) { }

  ngOnInit() {
    this.getBudget();
    this.app.setBackEnabled(false);
    this.app.setActionable(this)
    this.categoryBalances = new Map();
  }

  ngOnDestroy() {
    this.app.setActionable(null)
  }

  getBudget() {
    const id = this.route.snapshot.paramMap.get('id');
    this.twigsService.getBudget(id)
      .subscribe(budget => {
        this.app.setTitle(budget.name)
        this.budget = budget;
        this.getBalance();
        this.getTransactions();
        this.getCategories();
      });
  }

  updateBarChart() {
    const color = [0, 188, 212];
    this.barChartData = [
      {
        data: [this.expectedIncome / 100, this.expectedExpenses / 100],
        label: 'Expected',
        backgroundColor: 'rgba(241, 241, 241, 0.8)',
        borderColor: 'rgba(241, 241, 241, 0.9)',
        hoverBackgroundColor: 'rgba(241, 241, 241, 1)',
        hoverBorderColor: 'rgba(241, 241, 241, 1)',
      },
      {
        data: [this.actualIncome / 100, this.actualExpenses / 100],
        label: 'Actual',
        backgroundColor: `rgba(${color[0]}, ${color[1]}, ${color[2]}, 0.8)`,
        borderColor: `rgba(${color[0]}, ${color[1]}, ${color[2]}, 0.9)`,
        hoverBackgroundColor: `rgba(${color[0]}, ${color[1]}, ${color[2]}, 1)`,
        hoverBorderColor: `rgba(${color[0]}, ${color[1]}, ${color[2]}, 1)`
      }
    ];
  }

  getBalance(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.twigsService.getBudgetBalance(id).subscribe(balance => {
      this.budgetBalance = balance;
    });
  }

  getTransactions(): void {
    let date = new Date();
    date.setHours(0);
    date.setMinutes(0);
    date.setSeconds(0);
    date.setMilliseconds(0);
    date.setDate(1);
    this.twigsService.getTransactions(this.budget.id, null, 5, date)
      .subscribe(transactions => this.transactions = <Transaction[]>transactions);
  }

  getCategories(): void {
    this.twigsService.getCategories(this.budget.id).subscribe(categories => {
      const categoryBalances = new Map<string, number>();
      let categoryBalancesCount = 0;
      console.log(categories);
      for (const category of categories) {
        if (category.expense) {
          this.expenses.push(category);
          this.expectedExpenses += category.amount;
        } else {
          this.income.push(category);
          this.expectedIncome += category.amount;
        }
        this.twigsService.getCategoryBalance(category.id).subscribe(
          balance => {
            console.log(balance);
            if (category.expense) {
              this.actualExpenses += balance * -1;
            } else {
              this.actualIncome += balance;
            }
            categoryBalances.set(category.id, balance);
            categoryBalancesCount++;
          },
          error => { categoryBalancesCount++; },
          () => {
            // This weird workaround is to force the OnChanges callback to be fired.
            // Angular needs the reference to the object to change in order for it to
            // work.
            if (categoryBalancesCount === categories.length) {
              this.categoryBalances = categoryBalances;
              this.updateBarChart();
            }
          }
        );
      }
    });
  }

  doAction(): void {
    this.router.navigateByUrl(this.router.routerState.snapshot.url + "/edit")
  }

  getActionLabel(): string { 
    return "Edit";
  }
}
