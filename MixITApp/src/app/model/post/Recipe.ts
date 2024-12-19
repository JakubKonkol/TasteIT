import {Ingredient} from "./Ingredient";
import {IngredientWrapper} from "./IngredientWrapper";

export interface Recipe{
  steps?: Map<number, string>;
  pictures?: string[];
  ingredientsWithMeasurements?: Ingredient[];
}
