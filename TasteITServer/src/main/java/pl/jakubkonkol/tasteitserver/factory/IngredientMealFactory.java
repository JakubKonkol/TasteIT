package pl.jakubkonkol.tasteitserver.factory;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import pl.jakubkonkol.tasteitserver.model.Ingredient;

@Component
@RequiredArgsConstructor
public class IngredientMealFactory {
    public Ingredient createIngredient(JSONObject ingredientObj) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(ingredientObj.getString("strIngredient").toLowerCase());
        ingredient.setDescription(ingredientObj.optString("strDescription", "No description available"));
        ingredient.setType("Food");
        ingredient.setStrength("0");
        ingredient.setAlcohol(false);
        ingredient.setImageURL(null);
        return ingredient;
    }
}
