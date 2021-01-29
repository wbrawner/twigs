import { Component, OnInit, Input, Inject } from '@angular/core';
import { AppComponent } from '../app.component';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-transactions',
  templateUrl: './transactions.component.html',
  styleUrls: ['./transactions.component.css']
})
export class TransactionsComponent implements OnInit {

  budgetId?: string;
  categoryId?: string;

  constructor(
    private route: ActivatedRoute,
    private app: AppComponent,
  ) { }

  ngOnInit() {
    this.budgetId = this.route.snapshot.queryParamMap.get('budgetIds');
    this.categoryId = this.route.snapshot.queryParamMap.get('categoryIds');
    this.app.setBackEnabled(true);
    this.app.setTitle('Transactions')
  }
}
