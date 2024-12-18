import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {PostData} from "./shared/postData";
import {EPostType} from "../../model/post/EPostType";
import {Ingredient} from "../../model/post/Ingredient";

type StepNumber = 1 | 2 | 3 | 4 | 5;
@Component({
  selector: 'app-post-builder',
  templateUrl: './post-builder.component.html',
  styleUrls: ['./post-builder.component.css']
})
export class PostBuilderComponent implements OnInit{
  postData: PostData = {
    postMedia: {
      title: '',
      description: '',
      pictures: [],
    },
    recipe: {
      steps: new Map<number, string>(),
      pictures: [],
      ingredientsWithMeasurements: []
    },
    postType: EPostType.FOOD,
    tags: [],
  }
  picUrl: string = '';
  currentStep: StepNumber = 3;
  constructor(
    private router: Router,
  ) {}

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
  onUpdatePhoto(pictures: string[]){
    this.postData.postMedia.pictures = pictures;
    this.picUrl = pictures[0];
    this.nextStep();
  }
  onUpdateDetails(formData: any){
    this.postData.postMedia.title = formData.postMedia.title;
    this.postData.postMedia.description = formData.postMedia.description;
    this.postData.tags = formData.tags;
    this.postData.postType = formData.postType;
    this.nextStep();
  }
  onUpdateRecipe(recipe: PostData['recipe']){
    this.postData.recipe = recipe;
    this.nextStep();
  }
  onUpdateIngredients(ingredients: Ingredient[]){
    this.postData.recipe.ingredientsWithMeasurements = ingredients;
    this.nextStep();
  }
  onClose(){
    this.router.navigate(['/']);
  }
  async onSubmit(){
    console.log(this.postData);
  }

}
