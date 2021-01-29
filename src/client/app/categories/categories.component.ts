import { Component, OnInit, Input, Inject } from '@angular/core';
import { Category } from './category';
import { AppComponent } from '../app.component';
import { Observable } from 'rxjs';
import { TransactionType } from '../transactions/transaction.type';
import { Budget } from '../budgets/budget';
import { ActivatedRoute } from '@angular/router';
import { TWIGS_SERVICE, TwigsService } from '../shared/twigs.service';

@Component({
  selector: 'app-categories',
  templateUrl: './categories.component.html',
  styleUrls: ['./categories.component.css']
})
export class CategoriesComponent implements OnInit {

  budgetId: string;
  public categories: Category[];
  public categoryBalances: Map<string, number>;

  constructor(
    private route: ActivatedRoute,
    private app: AppComponent,
    @Inject(TWIGS_SERVICE) private twigsService: TwigsService,
  ) { }

  ngOnInit() {
    this.budgetId = this.route.snapshot.paramMap.get('budgetId');
    this.app.setTitle('Categories')
    this.app.setBackEnabled(true);
    this.getCategories();
    this.categoryBalances = new Map();
  }

  getCategories(): void {
    this.twigsService.getCategories(this.budgetId).subscribe(categories => {
      this.categories = categories;
      for (const category of this.categories) {
        this.getCategoryBalance(category).subscribe(balance => this.categoryBalances.set(category.id, balance));
      }
    });
  }

  getCategoryBalance(category: Category): Observable<number> {
    return Observable.create(subscriber => {
      this.twigsService.getTransactions(this.budgetId, category.id).subscribe(transactions => {
        let balance = 0;
        for (const transaction of transactions) {
          if (transaction.expense) {
            balance -= transaction.amount;
          } else {
            balance += transaction.amount;
          }
        }
        subscriber.next(balance);
      });
    });
  }
}
