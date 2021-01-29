import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { EditBudgetComponent } from './edit-budget.component';

describe('EditBudgetComponent', () => {
  let component: EditBudgetComponent;
  let fixture: ComponentFixture<EditBudgetComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ EditBudgetComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditBudgetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
