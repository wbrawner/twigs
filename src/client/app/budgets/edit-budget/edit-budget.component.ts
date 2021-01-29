import { Component, OnInit, Inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TwigsService, TWIGS_SERVICE } from '../../shared/twigs.service';
import { Budget } from '../budget';

@Component({
  selector: 'app-edit-budget',
  templateUrl: './edit-budget.component.html',
  styleUrls: ['./edit-budget.component.css']
})
export class EditBudgetComponent implements OnInit {

  budget: Budget;
  
  constructor(
    private route: ActivatedRoute,
    @Inject(TWIGS_SERVICE) private twigsService: TwigsService,
  ) { }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.twigsService.getBudget(id)
      .subscribe(budget => {
        this.budget = budget;
      });
  }
}
