import { Component, OnInit, Input, Inject, ChangeDetectorRef } from '@angular/core';
import { AppComponent } from '../app.component';
import { Budget } from './budget';
import { TWIGS_SERVICE, TwigsService } from '../shared/twigs.service';

@Component({
  selector: 'app-budgets',
  templateUrl: './budget.component.html',
  styleUrls: ['./budget.component.css']
})
export class BudgetsComponent implements OnInit {

  public budgets: Budget[];
  public loading = true;
  public loggedIn = false;

  constructor(
    private app: AppComponent,
    @Inject(TWIGS_SERVICE) private twigsService: TwigsService,  
  ) { }

  ngOnInit() {
    this.app.setBackEnabled(false);
    this.app.user.subscribe(
      user => {
        if (!user) {
          this.loading = false;
          this.loggedIn = false;
          this.app.setTitle('Welcome')
          return;
        }
        this.app.setTitle('Budgets')
        this.loggedIn = true;
        this.loading = true;
        this.twigsService.getBudgets().subscribe(
          budgets => {
            console.log(budgets)
            this.budgets = budgets;
            this.loading = false;
          },
          error => {
            this.loading = false;
          }
        );    
      },
      error => {
        this.loading = false;
      }
    )
  }
}
