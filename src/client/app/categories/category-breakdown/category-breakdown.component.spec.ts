import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { CategoryBreakdownComponent } from './category-breakdown.component';

describe('CategoryBreakdownComponent', () => {
  let component: CategoryBreakdownComponent;
  let fixture: ComponentFixture<CategoryBreakdownComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ CategoryBreakdownComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CategoryBreakdownComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
