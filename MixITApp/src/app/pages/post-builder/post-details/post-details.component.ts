import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {debounceTime, distinctUntilChanged} from 'rxjs/operators';
import {Tag} from "../../../model/user/Tag";
import {EPostType} from "../../../model/post/EPostType";
import {TagType} from "../../../model/user/TagType";
import {PostMedia} from "../../../model/post/PostMedia";
import {PostData} from "../shared/postData";

@Component({
  selector: 'app-post-details',
  templateUrl: './post-details.component.html',
  styleUrls: ['./post-details.component.css']
})
export class PostDetailsComponent implements OnInit {
  readyToContinue: boolean = false;
  postForm!: FormGroup;
  tagSearchControl = new FormControl('');
  availableTags: Tag[] = [];
  filteredTags: Tag[] = [];
  selectedTags: Tag[] = [];
  postTypes = ['Food', 'Drink']
  @Output() close = new EventEmitter<void>();
  @Output() nextStep = new EventEmitter<any>();
  @Output() prevStep = new EventEmitter<void>();
  constructor(private fb: FormBuilder) {}

  ngOnInit() {
    this.postForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      category: [null, Validators.required]
    });

    //placeholder
    this.availableTags = [
      { tagId: '1', tagName: 'Travel', tagType: TagType.DETAILED },
      { tagId: '2', tagName: 'Food', tagType: TagType.DETAILED },
      { tagId: '3', tagName: 'Lifestyle', tagType: TagType.DETAILED },
      { tagId: '4', tagName: 'Photography', tagType: TagType.DETAILED },
      { tagId: '5', tagName: 'Adventure', tagType: TagType.DETAILED }
    ];

    this.tagSearchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged()
      )
      .subscribe(value => this.filterTags(value as string));

    this.postForm.valueChanges.subscribe(value => {
      this.readyToContinue = this.postForm.valid;
    })
  }

  filterTags(searchTerm: string) {
    if (!searchTerm) {
      this.filteredTags = [];
      return;
    }

    this.filteredTags = this.availableTags
      .filter(tag =>
        tag.tagName.toLowerCase().includes(searchTerm.toLowerCase()) &&
        !this.selectedTags.some(selected => selected.tagId === tag.tagId)
      )
      .slice(0, 5);
  }

  addTag(tag: Tag) {
    if (!this.selectedTags.some(t => t.tagId === tag.tagId)) {
      this.selectedTags.push(tag);
      this.tagSearchControl.setValue('');
      this.filteredTags = [];
    }
  }

  removeTag(tag: Tag) {
    this.selectedTags = this.selectedTags.filter(t => t.tagId !== tag.tagId);
  }

  onTagSearch(event: Event) {
    const input = event.target as HTMLInputElement;
    this.filterTags(input.value);
  }
  onClose(){
    this.close.emit();
  }
  onContinue(){
    if(!this.postForm.valid) return;
    const postMedia: PostMedia = {
      title: this.postForm.value.title,
      description: this.postForm.value.description,
    }
    const tags = this.selectedTags; //This needs to be valid tag from the backend
    const formData = {postMedia, tags}
    this.nextStep.emit(formData);
  }

  onPrevStep() {
    this.prevStep.emit();
  }
}
