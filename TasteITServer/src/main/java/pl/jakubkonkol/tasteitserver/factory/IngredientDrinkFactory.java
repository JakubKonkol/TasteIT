package pl.jakubkonkol.tasteitserver.factory;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import pl.jakubkonkol.tasteitserver.model.Ingredient;


@Component
@RequiredArgsConstructor
public class IngredientDrinkFactory {
    public Ingredient createIngredient(JSONObject ingredientObj){
        var ingredient = new Ingredient();
        ingredient.setName(ingredientObj.getString("strIngredient").toLowerCase());
        ingredient.setDescription(ingredientObj.optString("strDescription"));
        ingredient.setType(ingredientObj.optString("strType"));
        String strAlcohol = ingredientObj.getString("strAlcohol");
        boolean isAlcohol = strAlcohol.equals("Yes");
        ingredient.setAlcohol(isAlcohol);
        ingredient.setStrength(ingredientObj.optString("strABV"));
        ingredient.setType("Drink");
        return ingredient;
    }
}
