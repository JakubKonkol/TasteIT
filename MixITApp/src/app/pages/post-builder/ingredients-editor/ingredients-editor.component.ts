import {Component, EventEmitter, OnDestroy, OnInit, Output} from '@angular/core';
import {PostBuilderModule} from "../shared/PostBuilderModule";
import {debounceTime, distinctUntilChanged, Subject, switchMap, takeUntil} from "rxjs";
import {Ingredient} from "../../../model/post/Ingredient";
import {Measurement} from "../../../model/post/Measurement";
import {SearchService} from "../../../service/search.service";
import {IngredientService} from "../../../service/ingredient.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {PostBuilderService} from "../shared/postBuilder.service";

@Component({
  selector: 'app-ingredients-editor',
  templateUrl: './ingredients-editor.component.html',
  styleUrls: ['./ingredients-editor.component.css']
})
export class IngredientsEditorComponent implements PostBuilderModule, OnInit, OnDestroy{
  @Output() close: EventEmitter<void> = new EventEmitter<void>();
  @Output() nextStep: EventEmitter<any> = new EventEmitter<any>();
  @Output() prevStep: EventEmitter<void> = new EventEmitter<void>();
  canProceed: boolean = false;
  private destroy$ = new Subject<void>();
  searchQuery = new Subject<string>();
  suggestions: Ingredient[] = [];
  selectedIngredients: Ingredient[] = [];
  showNewIngredientForm = false;
  showMeasurementModal = false;

  units = ['ml', 'g', 'pcs', 'tbsp', 'tsp', 'cup']; // This should be fetched from the server ig
  newIngredientForm: FormGroup;
  measurementForm: FormGroup;
  currentIngredient: Ingredient | null = null;
  errorMessage: string = '';
  showStrengthField = false;

  constructor(
    private searchService: SearchService,
    private ingredientService: IngredientService,
    private fb: FormBuilder,
    private postBuilderService: PostBuilderService
  ) {
    this.newIngredientForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      isAlcohol: [false],
      strength: [''],
    });

    this.measurementForm = this.fb.group({
      value: ['', Validators.required],
      unit: ['', Validators.required]
    });
  }
  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
  initializePostData() {
    const currentPostData = this.postBuilderService.getCurrentPostData();
    if (currentPostData.recipe.ingredientsWithMeasurements){
      this.selectedIngredients = currentPostData.recipe.ingredientsWithMeasurements;
    }
  }
  ngOnInit(): void {
    this.searchQuery.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(query => {
        if (query.length < 2) {
          this.suggestions = [];
          return [];
        }
        return this.searchService.searchIngredients(query, 0, 5);
      })
    ).subscribe(results => {
      this.suggestions = results;
    });
    this.initializePostData();
    this.updateCanProceed();
  }

  onSearch(event: Event) {
    const term = (event.target as HTMLInputElement).value;
    this.searchQuery.next(term);
  }

  selectIngredient(ingredient: Ingredient) {
    this.currentIngredient = ingredient;
    this.showMeasurementModal = true;
    this.suggestions = [];
  }

  addMeasurement() {
    if (this.measurementForm.valid && this.currentIngredient) {
      const ingredientWithMeasurement = {
        ...this.currentIngredient,
        measurement: this.measurementForm.value
      };
      this.selectedIngredients.push(ingredientWithMeasurement);
      this.showMeasurementModal = false;
      this.currentIngredient = null;
      this.measurementForm.reset();
      this.updateCanProceed();
    }
  }

  onClose(): void {
    this.close.emit();
  }
  onNextStep(): void {
    this.postBuilderService.updatePostData({
      recipe: {
        ingredientsWithMeasurements: this.selectedIngredients
      }
    });
    this.nextStep.emit(this.selectedIngredients);
  }
  onPrevStep(): void {
    this.prevStep.emit();
  }

  /**
   * Na ten moment backend nie zwraca żadnych danych po zapisaniu składnika.
   */
  saveNewIngredient() {
    if (this.newIngredientForm.valid) {
      this.errorMessage = '';
      this.ingredientService.saveIngredient(this.newIngredientForm.value)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (savedIngredient) => {
            if (!savedIngredient || !savedIngredient.ingredientId) {
              this.errorMessage = 'Ingredient was saved, but no data was returned. Please refresh to check.';
              console.warn('API did not return the saved ingredient:', savedIngredient);
              return;
            }
            this.currentIngredient = savedIngredient;
            this.showNewIngredientForm = false;
            this.showMeasurementModal = true;
            this.newIngredientForm.reset();
          },
          error: (error) => {
            this.errorMessage = 'Failed to save ingredient. Please try again.';
            console.error('Error saving ingredient:', error);
          }
        });
    }
  }
  updateCanProceed() {
    this.canProceed = this.selectedIngredients.length > 0;
  }
  deleteSelectedIngredient(index: number) {
    this.selectedIngredients.splice(index, 1);
    this.updateCanProceed();
  }

  toggleStrengthField() {
    this.showStrengthField = this.newIngredientForm.get('isAlcohol')?.value;
    if (!this.showStrengthField) {
      this.newIngredientForm.get('strength')?.reset();
    }
  }
}
