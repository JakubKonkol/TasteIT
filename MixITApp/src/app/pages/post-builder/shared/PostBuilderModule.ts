import {EventEmitter} from "@angular/core";

export interface PostBuilderModule{
  close: EventEmitter<void>;
  nextStep: EventEmitter<any>;
  prevStep: EventEmitter<void>;

  onClose(): void;
  onPrevStep?(): void;
  onContinue?(): void;
}
