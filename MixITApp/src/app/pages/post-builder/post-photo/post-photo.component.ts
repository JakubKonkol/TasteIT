import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ImageCroppedEvent} from "ngx-image-cropper";
import {StorageUploadService} from "../../../service/storage-upload.service";
import {UserService} from "../../../service/user.service";
import {User} from "../../../model/user/User";
import {PhotoHelper} from "../../../helpers/PhotoHelper";
import {PostData} from "../shared/postData";
import {PostBuilderModule} from "../shared/PostBuilderModule";

@Component({
  selector: 'app-post-photo',
  templateUrl: './post-photo.component.html',
  styleUrls: ['./post-photo.component.css']
})
export class PostPhotoComponent implements OnInit{
  imageChangedEvent: any = '';
  croppedImage: any;
  picUrl: string = '';
  user: User = {}
  readyToContinue: boolean =false;
  @Input() postData!: PostData;
  @Output() imageUploaded = new EventEmitter<string[]>();
  @Output() close = new EventEmitter<void>();
  constructor(private storageUploadService: StorageUploadService,
              private userService: UserService,
              )
  {}
  async ngOnInit() {
    this.user = await this.userService.getUserByToken();
  }

  onFileSelected($event: Event): void {
    const target = $event.target as HTMLInputElement;
    if (target.files && target.files.length > 0) {
      this.imageChangedEvent = $event;
    }
  }

  onImageCropped($event: ImageCroppedEvent): void {
    this.croppedImage = $event.base64;
    this.readyToContinue = true;
  }
  uploadPhoto(): void {
    if (!this.croppedImage) return;

    const file = PhotoHelper.base64ToFile(this.croppedImage, `${this.user.userId}.png`);
    const randomUUID = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
    const filePath = `post_pictures/${randomUUID}.png`;

    this.storageUploadService.uploadFile(file, filePath).subscribe(url => {
      this.picUrl = url;
      this.imageUploaded.emit([this.picUrl]);
    });
  }
  skipToNextStep(): void {
    this.imageUploaded.emit([]);
  }


  onClose() {
    if(this.picUrl) this.storageUploadService.deleteFile(this.picUrl)
    this.close.emit();
  }
}
