import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
  selector: 'app-pbnavigation',
  templateUrl: './pbnavigation.component.html',
  styleUrls: ['./pbnavigation.component.css']
})
export class PBNavigationComponent {
  @Input() currentStep: number = 1;
  @Input() canProceed: boolean = false;
  @Input() isLastStep: boolean = false;
  @Output() closeEmitter = new EventEmitter<void>();
  @Output() prevStepEmitter = new EventEmitter<void>();
  @Output() nextStepEmitter = new EventEmitter<void>();


  prevStep(){
    this.prevStepEmitter.emit();
  }
  nextStep(){
    this.nextStepEmitter.emit();
  }
  close(){
    this.closeEmitter.emit();
  }

  protected readonly onsubmit = onsubmit;

  onSubmit() {
    this.nextStepEmitter.emit();
  }
}
