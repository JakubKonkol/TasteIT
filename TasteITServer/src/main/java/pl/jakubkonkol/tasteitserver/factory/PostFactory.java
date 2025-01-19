package pl.jakubkonkol.tasteitserver.factory;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import pl.jakubkonkol.tasteitserver.model.*;
import pl.jakubkonkol.tasteitserver.model.enums.TagType;
import pl.jakubkonkol.tasteitserver.service.interfaces.IIngredientService;
import pl.jakubkonkol.tasteitserver.service.interfaces.ITagService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public abstract class PostFactory {
    protected IIngredientService ingredientService;
    protected ITagService tagService;

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
            System.out.println("ingredient name " + ingredientName);
//            Optional<Ingredient> optionalIngredient;
//            try {
                var optionalIngredient = ingredientService.findByName(ingredientName);
//            } catch (Exception e) {
//                System.out.println(e.getMessage());
//            }
            if (optionalIngredient.isPresent()) {
                var ingredient = optionalIngredient.get();
                ingWrapper = ingredientService.convertToWrapper(ingredient);
            } else {
                var ingredient = new Ingredient();
                ingredient.setName(ingredientName);
                ingWrapper = ingredientService.convertToWrapper(ingredient);
            }

            var measure = new Measurement();
            measure.setValue(ingredientAmount);
            measure.setUnit("unit");
            ingWrapper.setMeasurement(measure);
            ingredients.add(ingWrapper);
        }
        return ingredients;
    }
    protected List<Tag> createTags(JSONObject postObj){
        List<Tag> tags = new ArrayList<>();
        var tagsArray = postObj.optString("strTags", "").split(",");

        for (String tag: tagsArray){
            var optionalTag = tagService.findByName(tag);
            if (optionalTag.isPresent()){
                tags.add(optionalTag.get());
            } else {
                var newTag = new Tag();
                newTag.setTagName(tag);
                newTag.setTagType(TagType.DETAILED);
                var savedTag = tagService.save(newTag);
                tags.add(savedTag);
            }
        }

        return tags;
    }

    public abstract Post createPost(JSONObject postObj);
}
