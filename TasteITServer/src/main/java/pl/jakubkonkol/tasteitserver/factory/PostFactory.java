package pl.jakubkonkol.tasteitserver.factory;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.jakubkonkol.tasteitserver.model.*;
import pl.jakubkonkol.tasteitserver.service.IngredientService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public abstract class PostFactory {
    @Autowired
    protected IngredientService ingredientService;

    protected PostMedia createPostMedia(JSONObject postObj, String titleKey, String thumbKey, String defaultDescription) {
        PostMedia postMedia = new PostMedia();
        postMedia.setTitle(postObj.getString(titleKey));
        postMedia.setDescription(defaultDescription);
        postMedia.setPictures(List.of(postObj.getString(thumbKey)));
        return postMedia;
    }

    protected Recipe createRecipe(JSONObject postObj, String instructionsKey) {
        Recipe recipe = new Recipe();
        String strInstructions = postObj.getString(instructionsKey);
        String[] splittedInstructionsArray = strInstructions.split("\\.");
        Map<Integer, String> splittedInstructionsMap = IntStream.range(0, splittedInstructionsArray.length)
                .boxed()
                .collect(Collectors.toMap(
                        i -> i + 1,
                        i -> splittedInstructionsArray[i].trim()
                ));
        recipe.setSteps(splittedInstructionsMap);
        recipe.setPictures(null);
        return recipe;
    }

    protected List<IngredientWrapper> createIngredients(JSONObject postObj) {
        List<IngredientWrapper> ingredients = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            var ingWrapper = new IngredientWrapper();
            String ingredientName = postObj.optString("strIngredient" + i, "").toLowerCase();
            String ingredientAmount = postObj.optString("strMeasure" + i, "").trim();

            if (ingredientName.isBlank() || ingredientAmount.isBlank()) break;

            var measure = new Measurement();
            measure.setValue(ingredientAmount);
            measure.setUnit("unit");
            ingWrapper.setMeasurement(measure);
            if (this.ingredientService.findByName(ingredientName).isPresent()) {
                var ingredient = this.ingredientService.findByName(ingredientName).get();
                ingWrapper.setIngredient(ingredient);
            } else {
                var ingredient = new Ingredient();
                ingredient.setName(ingredientName);
                ingWrapper.setIngredient(ingredient);
            }
            ingredients.add(ingWrapper);
        }
        return ingredients;
    }

    public abstract Post createPost(JSONObject postObj);
}
