import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {debounceTime, distinctUntilChanged} from 'rxjs/operators';
import {Tag} from "../../../model/user/Tag";
import {EPostType} from "../../../model/post/EPostType";
import {TagService} from "../../../service/tag.service";
import {PostBuilderModule} from "../shared/PostBuilderModule";
import {TagType} from "../../../model/user/TagType";
import {PostBuilderService} from "../shared/postBuilder.service";

@Component({
  selector: 'app-post-details',
  templateUrl: './post-details.component.html',
  styleUrls: ['./post-details.component.css']
})
export class PostDetailsComponent implements PostBuilderModule, OnInit {
  readyToContinue: boolean = false;
  postForm!: FormGroup;
  tagSearchControl = new FormControl('');
  availableTags: Tag[] = [];
  filteredTags: Tag[] = [];
  selectedTags: Tag[] = [];
  isCreatingTag: boolean = false;
  searchTerm: string = '';
  postTypes = Object.values(EPostType);

  @Output() close = new EventEmitter<void>();
  @Output() nextStep = new EventEmitter<any>();
  @Output() prevStep = new EventEmitter<void>();

  constructor(
    private fb: FormBuilder,
    private tagService: TagService,
    private postBuilderService: PostBuilderService
  ) {}

  async ngOnInit() {
    this.initializeForm();
    await this.loadTags();
    this.setupTagSearch();
    this.setupFormValidation();
    this.loadInitialData();
  }
  private loadInitialData() {
    const currentPostData = this.postBuilderService.getCurrentPostData();
    if (currentPostData.postMedia) {
      this.postForm.patchValue({
        title: currentPostData.postMedia.title,
        description: currentPostData.postMedia.description,
        category: currentPostData.postType
      });
      if (currentPostData.tags) {
        this.selectedTags = currentPostData.tags;
      }
    }
  }

  private initializeForm() {
    this.postForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(5)]],
      category: [null, Validators.required]
    });
  }

  private async loadTags() {
    try {
      this.availableTags = (await this.tagService.getAll()).filter(tag => tag && tag.tagName);
    } catch (error) {
      console.error('Failed to load tags:', error);
      this.availableTags = [];
    }
  }

  private setupTagSearch() {
    this.tagSearchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged()
      )
      .subscribe(value => {
        this.searchTerm = value as string;
        this.filterTags(this.searchTerm);
      });
  }

  private setupFormValidation() {
    this.postForm.valueChanges.subscribe(() => {
      this.readyToContinue = this.postForm.valid;
    });
  }

  filterTags(searchTerm: string) {
    if (!searchTerm?.trim()) {
      this.filteredTags = [];
      this.isCreatingTag = false;
      return;
    }

    const normalizedSearchTerm = searchTerm.toLowerCase().trim();

    this.filteredTags = this.availableTags
      .filter(tag => {
        if (!tag?.tagName) return false;

        return tag.tagName.toLowerCase().includes(normalizedSearchTerm) &&
          !this.selectedTags.some(selected => selected.tagId === tag.tagId);
      })
      .slice(0, 5);

    const exactMatch = this.availableTags.some(tag =>
      tag?.tagName?.toLowerCase() === normalizedSearchTerm
    );
    this.isCreatingTag = !exactMatch && normalizedSearchTerm.length > 0;
  }

  async createAndAddTag(tagName: string) {
    if (!tagName?.trim()) return;
    const tagToCreate: Tag = {
      tagName: tagName.trim(),
      tagType: TagType.DETAILED
    }
    try {
      const newTag = await this.tagService.create(tagToCreate);

      if (newTag && newTag.tagId && newTag.tagName) {
        this.availableTags.push(newTag);
        this.addTag(newTag);
      } else {
        console.error('Received invalid tag from server:', newTag);
      }
    } catch (error) {
      console.error('Failed to create tag:', error);
    }
  }

  addTag(tag: Tag) {
    if (!tag?.tagId || !tag?.tagName) return;

    if (!this.selectedTags.some(t => t.tagId === tag.tagId)) {
      this.selectedTags.push(tag);
      this.tagSearchControl.setValue('');
      this.filteredTags = [];
      this.isCreatingTag = false;
    }
  }

  removeTag(tag: Tag) {
    if (!tag?.tagId) return;
    this.selectedTags = this.selectedTags.filter(t => t.tagId !== tag.tagId);
  }

  onTagSearch(event: Event) {
    const input = event.target as HTMLInputElement;
    this.searchTerm = input.value;
    this.filterTags(this.searchTerm);
  }

  onClose() {
    this.close.emit();
  }

  getFormValue() {
    if (!this.postForm.valid) return null;

    return {
      postMedia: {
        title: this.postForm.value.title,
        description: this.postForm.value.description,
      },
      tags: this.selectedTags,
      postType: this.postForm.value.category
    };
  }

  onContinue() {
    const formValue = this.getFormValue();
    if (formValue) {
      this.postBuilderService.updatePostData({
        postMedia: {
          title: formValue.postMedia.title,
          description: formValue.postMedia.description
        },
        tags: formValue.tags,
        postType: formValue.postType
      });
      this.nextStep.emit(formValue);
    }
  }


  onPrevStep() {
    this.prevStep.emit();
  }
}
