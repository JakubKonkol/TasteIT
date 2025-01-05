import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PBNavigationComponent } from './pbnavigation.component';

describe('PBNavigationComponent', () => {
  let component: PBNavigationComponent;
  let fixture: ComponentFixture<PBNavigationComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PBNavigationComponent]
    });
    fixture = TestBed.createComponent(PBNavigationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
