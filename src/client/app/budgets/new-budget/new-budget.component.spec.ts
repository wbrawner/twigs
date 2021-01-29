import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { NewBudgetComponent } from './new-budget.component';

describe('NewBudgetComponent', () => {
  let component: NewBudgetComponent;
  let fixture: ComponentFixture<NewBudgetComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ NewBudgetComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NewBudgetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
