import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Tag} from '../../model/user/Tag';
import {HotToastService} from '@ngneat/hot-toast';
import {UserProfile} from '../../model/user/UserProfile';
import {UserService} from '../../service/user.service';
import {User} from '../../model/user/User';
import {Router} from '@angular/router';
import {UserTags} from '../../model/user/UserTags';
import {StorageUploadService} from '../../service/storage-upload.service';
import {ImageCroppedEvent} from 'ngx-image-cropper';
import {TagService} from "../../service/tag.service";
import {PhotoHelper} from "../../helpers/PhotoHelper";

interface Step {
  label: string;
  description: string;
  isValid: () => boolean;
}

@Component({
  selector: 'app-setup-profile',
  templateUrl: './setup-profile.component.html',
  styleUrls: ['./setup-profile.component.css'],
})
export class SetupProfileComponent implements OnInit {
  form: FormGroup;
  currentStep: number = 1;
  imageChangedEvent: any = '';
  croppedImage: any;
  preferences: Tag[] = [];
  user!: User;
  profilePicUrl: string = '';

  tags: Tag[] = [];

  steps: Step[] = [
    {
      label: 'How should we call you?',
      description: 'This is the name that will be displayed on your profile.',
      isValid: () => this.form.get('username')?.valid || false,
    },
    {
      label: 'Add a profile picture',
      description: 'Upload a photo that will help others recognize you on TasteIT.',
      isValid: () => true,
    },
    {
      label: 'When\'s your birthday?',
      description: 'This helps us personalize your experience and ensure you\'re the right age to join.',
      isValid: () => this.form.get('birthdate')?.valid || false,
    },
    {
      label: 'Tell us about yourself',
      description: 'You can skip this if nothing comes to mind right now.',
      isValid: () => true,
    },
    {
      label: 'What topics are you interested in?',
      description: 'Choose at least three to help us better tailor experience for you.',
      isValid: () => this.preferences.length >= 3,
    },
    {
      label: 'Success!',
      description: 'Your profile is complete! You now have access to all features of TasteIT.',
      isValid: () => true,
    },
  ];

  constructor(
    private fb: FormBuilder,
    private hotToast: HotToastService,
    private userService: UserService,
    private router: Router,
    private storageUploadService: StorageUploadService,
    private tagService: TagService,
  ) {
    this.form = this.fb.group({
      username: ['', Validators.required],
      birthdate: ['', Validators.required],
      bio: [''],
    });
  }

  async ngOnInit(): Promise<void> {
    this.user = await this.userService.getUserByToken();
    this.tags = await this.tagService.getBasicTags();
  }

  getProgress(): number {
    return (this.currentStep / this.steps.length) * 100;
  }

  goNext(): void {
    if (this.isCurrentStepValid()) {
      if (this.currentStep === 2) {
        this.uploadProfilePicture();
      }
      this.currentStep++;
    } else {
      this.triggerValidationError();
      this.form.markAllAsTouched();
    }
  }

  goToPreviousStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
    }
  }

  isCurrentStepValid(): boolean {
    return this.steps[this.currentStep - 1].isValid();
  }

  triggerValidationError(): void {
    this.hotToast.error('Please fill out the required fields');
  }

  isSelected(tag: Tag): boolean {
    return this.preferences.includes(tag);
  }

  toggleTagSelection(tag: Tag): void {
    this.isSelected(tag) ? this.removeTag(tag) : this.addTag(tag);
  }

  removeTag(tag: Tag): void {
    this.preferences = this.preferences.filter(t => t.tagId !== tag.tagId);
  }

  addTag(tag: Tag): void {
    this.preferences.push(tag);
  }

  async setUpAccount(): Promise<void> {
    const userProfile: UserProfile = this.createUserProfile();
    const userTags: UserTags = this.createUserTags();

    await this.updateUserAccount(userTags, userProfile);
  }

  createUserProfile(): UserProfile {
    const birthdateInput = this.form.get('birthdate')?.value;
    const formattedDate = this.formatBirthdate(new Date(birthdateInput));

    return {
      userId: this.user.userId!,
      bio: this.form.get('bio')?.value,
      displayName: this.form.get('username')?.value,
      profilePicture: this.profilePicUrl,
      birthDate: formattedDate,
    };
  }

  formatBirthdate(birthdate: Date): string {
    const day = birthdate.getDate().toString().padStart(2, '0');
    const month = (birthdate.getMonth() + 1).toString().padStart(2, '0');
    const year = birthdate.getFullYear().toString();
    return `${year}-${month}-${day}`;
  }

  createUserTags(): UserTags {
    return {
      tags: this.preferences,
    };
  }

  async updateUserAccount(userTags: UserTags, userProfile: UserProfile): Promise<void> {
    try {
      await this.userService.updateUserTags(this.user.userId!, userTags);
      await this.userService.updateUserProfile(this.user.userId!, userProfile);
      await this.userService.changeUserFirstLogin(this.user.userId!);
      this.hotToast.success('Account setup complete');
      await this.router.navigate(['/home']);
    } catch (err) {
      console.error(err);
      this.hotToast.error('An error occurred while setting up your account');
    }
  }

  onFileSelected($event: Event): void {
    const target = $event.target as HTMLInputElement;
    if (target.files && target.files.length > 0) {
      this.imageChangedEvent = $event;
    }
  }

  onImageCropped($event: ImageCroppedEvent): void {
    this.croppedImage = $event.base64;
  }

  uploadProfilePicture(): void {
    if (!this.croppedImage) return;

    const file = PhotoHelper.base64ToFile(this.croppedImage, `${this.user.userId}.png`);
    const filePath = `profile_pictures/${this.user.userId}.png`;

    this.storageUploadService.uploadFile(file, filePath).subscribe(url => {
      this.profilePicUrl = url;
    });
  }
}
