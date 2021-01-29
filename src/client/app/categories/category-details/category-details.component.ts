import { Component, OnInit, Inject, ApplicationModule, OnDestroy } from '@angular/core';
import { Category } from '../category';
import { ActivatedRoute, Router } from '@angular/router';
import { TWIGS_SERVICE, TwigsService } from '../../shared/twigs.service';
import { Transaction } from '../../transactions/transaction';
import { AppComponent } from '../../app.component';
import { Actionable } from '../../shared/actionable';

@Component({
  selector: 'app-category-details',
  templateUrl: './category-details.component.html',
  styleUrls: ['./category-details.component.css']
})
export class CategoryDetailsComponent implements OnInit, OnDestroy, Actionable {

  budgetId: string;
  category: Category;
  public transactions: Transaction[];

  constructor(
    private route: ActivatedRoute,
    private app: AppComponent,
    @Inject(TWIGS_SERVICE) private twigsService: TwigsService,
    private router: Router
  ) { }

  doAction(): void {
    this.router.navigateByUrl(this.router.routerState.snapshot.url + "/edit")
  }

  getActionLabel(): string { 
    return "Edit";
  }

  ngOnInit() {
    this.app.setBackEnabled(true);
    this.app.setActionable(this)
    this.getCategory();
  }

  ngOnDestroy() {
    this.app.setActionable(null)
  }

  getCategory(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.twigsService.getCategory(id)
      .subscribe(category => {
        category.amount /= 100;
        this.app.setTitle(category.title)
        this.category = category;
        this.budgetId = category.budgetId;
      });
  }
}
