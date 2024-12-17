import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {PostBuilderModule} from "../shared/PostBuilderModule";
import {PostData} from "../shared/postData";
import {Observable} from "rxjs";
import {PostBuilderService} from "../shared/postBuilder.service";

@Component({
  selector: 'app-post-summary',
  templateUrl: './post-summary.component.html',
  styleUrls: ['./post-summary.component.css']
})
export class PostSummaryComponent implements PostBuilderModule, OnInit{
  @Output() close: EventEmitter<void> = new EventEmitter<void>();
  @Output() nextStep: EventEmitter<any> = new EventEmitter<any>();
  @Output() prevStep: EventEmitter<void> = new EventEmitter<void>();
  canProceed: boolean = true;

  constructor(private postBuilderService: PostBuilderService) {
  }

  ngOnInit(): void {
  }
  getPostData() {
    return this.postBuilderService.getCurrentPostData();
  }
  onClose(): void {
    this.close.emit();
  }
  onContinue() {
    console.log('Continue inside post-summary');
    this.nextStep.emit();
  }
  onPrevStep() {
    this.prevStep.emit();
  }

}
