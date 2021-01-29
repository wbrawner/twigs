import { Component, OnInit, Input } from '@angular/core';
import { Category } from '../category';

@Component({
  selector: 'app-category-list',
  templateUrl: './category-list.component.html',
  styleUrls: ['./category-list.component.css']
})
export class CategoryListComponent implements OnInit {

  @Input() budgetId: string;
  @Input() categories: Category[];
  @Input() categoryBalances: Map<string, number>;

  constructor() { }

  ngOnInit() {
  }

  getCategoryRemainingBalance(category: Category): number {
    let categoryBalance = this.categoryBalances.get(category.id);
    if (!categoryBalance) {
      categoryBalance = 0;
    }

    if (category.expense) {
      return (category.amount / 100) + (categoryBalance / 100);
    } else {
      return (category.amount / 100) - (categoryBalance / 100);
    }
  }

  getCategoryCompletion(category: Category): number {
    const amount = category.amount > 0 ? category.amount : 1;

    let categoryBalance = this.categoryBalances.get(category.id);
    if (!categoryBalance) {
      categoryBalance = 0;
    }

    // Invert the negative/positive values for calculating progress
    // since the limit for a category is saved as a positive but the
    // balance is used in the calculation.
    if (category.expense) {
      if (categoryBalance < 0) {
        categoryBalance = Math.abs(categoryBalance);
      } else {
        categoryBalance -= (categoryBalance * 2);
      }
    }

    return categoryBalance / amount * 100;
  }
}
