import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { NewCategoryComponent } from './new-category.component';

describe('NewCategoryComponent', () => {
  let component: NewCategoryComponent;
  let fixture: ComponentFixture<NewCategoryComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ NewCategoryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NewCategoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
