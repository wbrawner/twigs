import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { CategoryDetailsComponent } from './category-details.component';

describe('CategoryDetailsComponent', () => {
  let component: CategoryDetailsComponent;
  let fixture: ComponentFixture<CategoryDetailsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ CategoryDetailsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CategoryDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
