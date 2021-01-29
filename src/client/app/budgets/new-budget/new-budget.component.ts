import { Component, OnInit } from '@angular/core';
import { Budget } from '../budget';

@Component({
    selector: 'app-new-budget',
    templateUrl: './new-budget.component.html',
    styleUrls: ['./new-budget.component.css']
})
export class NewBudgetComponent implements OnInit {

    public budget: Budget;

    constructor() {
        this.budget = new Budget();
    }

    ngOnInit() {
    }

}
