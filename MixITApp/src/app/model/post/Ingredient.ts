import {Measurement} from "./Measurement";

export interface Ingredient{
  ingredientId?: number;
  name: string;
  description?: string;
  type?: string;
  isAlcohol?: boolean;
  strength?: string;
  imageURL?: string;
  measurement?: Measurement;
}
