import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PostPhotoComponent } from './post-photo.component';

describe('PostPhotoComponent', () => {
  let component: PostPhotoComponent;
  let fixture: ComponentFixture<PostPhotoComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PostPhotoComponent]
    });
    fixture = TestBed.createComponent(PostPhotoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
