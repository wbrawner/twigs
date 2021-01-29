import { Component, OnInit, Input, OnDestroy, Inject } from '@angular/core';
import { Category } from '../category';
import { AppComponent } from 'src/client/app/app.component';
import { TWIGS_SERVICE, TwigsService } from 'src/client/app/shared/twigs.service';

@Component({
  selector: 'app-category-form',
  templateUrl: './category-form.component.html',
  styleUrls: ['./category-form.component.css']
})
export class CategoryFormComponent implements OnInit {

  @Input() budgetId: string;
  @Input() title: string;
  @Input() currentCategory: Category;
  @Input() create: boolean;

  constructor(
    private app: AppComponent,
    @Inject(TWIGS_SERVICE) private twigsService: TwigsService,
  ) { }

  ngOnInit() {
    this.app.setBackEnabled(true);
    this.app.setTitle(this.title)
  }

  save(): void {
    let observable;
    if (this.create) {
      // This is a new category, save it
      observable = this.twigsService.createCategory(
        this.currentCategory.id,
        this.budgetId,
        this.currentCategory.title,
        this.currentCategory.description,
        this.currentCategory.amount * 100,
        this.currentCategory.expense
      );
    } else {
      // This is an existing category, update it
      observable = this.twigsService.updateCategory(
        this.currentCategory.id,
        {
          name: this.currentCategory.title,
          description: this.currentCategory.description,
          amount: this.currentCategory.amount * 100,
          expense: this.currentCategory.expense,
          archived: this.currentCategory.archived
        }
      );
    }
    observable.subscribe(val => {
      this.app.goBack();
    });
  }

  delete(): void {
    this.twigsService.deleteCategory(this.currentCategory.id).subscribe(() => {
      this.app.goBack();
    });
  }
}
