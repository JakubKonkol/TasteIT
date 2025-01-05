import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IngredientsEditorComponent } from './ingredients-editor.component';

describe('IngredientsEditorComponent', () => {
  let component: IngredientsEditorComponent;
  let fixture: ComponentFixture<IngredientsEditorComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [IngredientsEditorComponent]
    });
    fixture = TestBed.createComponent(IngredientsEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
