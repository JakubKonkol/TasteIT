package pl.jakubkonkol.tasteitserver.factory;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.jakubkonkol.tasteitserver.dto.IngredientDto;
import pl.jakubkonkol.tasteitserver.model.*;
import pl.jakubkonkol.tasteitserver.service.IngredientService;
import pl.jakubkonkol.tasteitserver.service.TagService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public abstract class PostFactory {
    @Autowired
    protected IngredientService ingredientService;
    @Autowired
    protected TagService tagService;

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

    protected List<IngredientDto> createIngredients(JSONObject postObj) {
        List<IngredientDto> ingredients = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            var ingDto = new IngredientDto();
            String ingredientName = postObj.optString("strIngredient" + i, "").toLowerCase();
            String ingredientAmount = postObj.optString("strMeasure" + i, "").trim();

            if (ingredientName.isBlank() || ingredientAmount.isBlank()) break;

            var optionalIngredient = this.ingredientService.findByName(ingredientName);
            if (optionalIngredient.isPresent()) {
                var ingredient = optionalIngredient.get();
                ingDto = ingredientService.convertToDto(ingredient);
            } else {
                var ingredient = new Ingredient();
                ingredient.setName(ingredientName);
                ingDto = ingredientService.convertToDto(ingredient);
            }

            var measure = new Measurement();
            measure.setValue(ingredientAmount);
            measure.setUnit("unit");
            ingDto.setMeasurement(measure);
            ingredients.add(ingDto);
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
                var savedTag = tagService.save(newTag);
                tags.add(savedTag);
            }
        }

        return tags;
    }

    public abstract Post createPost(JSONObject postObj);
}
