import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { NewTransactionComponent } from './new-transaction.component';

describe('NewTransactionComponent', () => {
  let component: NewTransactionComponent;
  let fixture: ComponentFixture<NewTransactionComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ NewTransactionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NewTransactionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
