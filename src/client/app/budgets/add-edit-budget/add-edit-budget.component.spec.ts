import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { AddEditBudgetComponent } from './add-edit-budget.component';

describe('AddEditBudgetComponent', () => {
  let component: AddEditBudgetComponent;
  let fixture: ComponentFixture<AddEditBudgetComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ AddEditBudgetComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AddEditBudgetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
