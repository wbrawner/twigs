import { Component, OnInit } from '@angular/core';
import { Category } from '../category';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-new-category',
  templateUrl: './new-category.component.html',
  styleUrls: ['./new-category.component.css']
})
export class NewCategoryComponent implements OnInit {

  budgetId: string;
  category: Category;

  constructor(
    private route: ActivatedRoute
  ) { }

  ngOnInit() {
    this.budgetId = this.route.snapshot.paramMap.get('budgetId');
    this.category = new Category();
    // TODO: Set random color for category, improve color picker
    // this.category.color =
  }

}
