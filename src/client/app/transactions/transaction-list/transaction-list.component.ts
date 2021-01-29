import { Component, OnInit, Input, Inject } from '@angular/core';
import { Transaction } from '../transaction';
import { TWIGS_SERVICE, TwigsService } from '../../shared/twigs.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-transaction-list',
  templateUrl: './transaction-list.component.html',
  styleUrls: ['./transaction-list.component.css']
})
export class TransactionListComponent implements OnInit {

  @Input() budgetIds: string[];
  @Input() categoryIds?: string[];
  public transactions: Transaction[];

  constructor(
    @Inject(TWIGS_SERVICE) private twigsService: TwigsService,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.getTransactions();
  }

  getTransactions(): void {
    let fromStr = this.route.snapshot.queryParamMap.get('from');
    var from;
    if (fromStr) {
      let fromDate = new Date(fromStr);
      if (!isNaN(fromDate.getTime())) {
        from = fromDate;
      }
    }

    if (!from) {
      let date = new Date();
      date.setHours(0);
      date.setMinutes(0);
      date.setSeconds(0);
      date.setMilliseconds(0);
      date.setDate(1);
      from = date;
    }

    let toStr = this.route.snapshot.queryParamMap.get('to');
    var to;
    if (toStr) {
      let toDate = new Date(toStr);
      if (!isNaN(toDate.getTime())) {
        to = toDate;
      }
    }

    this.twigsService.getTransactions(this.budgetIds.join(','), this.categoryIds?.join(','), null, from, to).subscribe(transactions => {
      this.transactions = transactions;
    });
  }
}
