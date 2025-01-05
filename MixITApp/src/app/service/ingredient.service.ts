import { Injectable } from "@angular/core";
import { LoggerService } from "./logger.service";
import { Ingredient } from "../model/post/Ingredient";
import taste_api from "../api/taste_api";
import { from, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class IngredientService {
  constructor(private logger: LoggerService) {
  }

  saveIngredient(ingredient: Ingredient): Observable<Ingredient> {
    return from(
      taste_api.post('ingredient/', ingredient)
        .then(res => res.data as Ingredient)
        .catch(error => {
          this.logger.logError(`Error saving ingredient`, error.response?.data || error);
          throw error.response?.data || error;
        })
    );
  }

  getAll(): Observable<Ingredient[]> {
    return from(
      taste_api.get('ingredient/')
        .then(res => res.data as Ingredient[])
        .catch(error => {
          this.logger.logError(`Error fetching all ingredients`, error.response?.data || error);
          throw error.response?.data || error;
        })
    );
  }
}
