import { Component, OnInit } from '@angular/core';
import { Transaction } from '../transaction';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-new-transaction',
  templateUrl: './new-transaction.component.html',
  styleUrls: ['./new-transaction.component.css']
})
export class NewTransactionComponent implements OnInit {

  budgetId: string;
  transaction: Transaction;

  constructor(
    private route: ActivatedRoute
  ) { }

  ngOnInit() {
    this.budgetId = this.route.snapshot.queryParamMap.get('budgetId');
    this.transaction = new Transaction();
  }

}
