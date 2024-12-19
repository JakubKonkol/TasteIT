import { Component, OnInit } from '@angular/core';
import { Router } from "@angular/router";
import { PostData } from "./shared/postData";
import { Observable } from "rxjs";
import { PostBuilderService } from "./shared/postBuilder.service";
import {PostService} from "../../service/post.service";

type StepNumber = 1 | 2 | 3 | 4 | 5;

@Component({
  selector: 'app-post-builder',
  templateUrl: './post-builder.component.html',
  styleUrls: ['./post-builder.component.css']
})
export class PostBuilderComponent implements OnInit {
  postData$: Observable<PostData>;
  picUrl: string = '';
  currentStep: StepNumber = 1;

  constructor(
    private router: Router,
    private postBuilderService: PostBuilderService,
    private postService: PostService
  ) {
    this.postData$ = this.postBuilderService.postData$;
  }

  async ngOnInit(): Promise<void> {

  }

  nextStep(): void {
    if (this.currentStep < 5) {
      this.currentStep = (this.currentStep + 1) as StepNumber;
    }
  }

  prevStep(): void {
    if (this.currentStep > 1) {
      this.currentStep = (this.currentStep - 1) as StepNumber;
    }
  }

  onUpdatePhoto(pictures: string[]) {
    this.postBuilderService.updatePostData({
      postMedia: {
        pictures: pictures
      }
    });
    this.picUrl = pictures[0];
    this.nextStep();
  }
  onClose() {
    this.postBuilderService.resetPostData();
    this.router.navigate(['/']);
  }

  async onSubmit() {
    const postData = this.postBuilderService.getCurrentPostData();
    console.log(postData);
    this.postService.createPost(postData).then((post) => {
      const postId = post.postId;
      this.postBuilderService.resetPostData();
      this.router.navigate(['/drink', postId]);
    })
  }
}
