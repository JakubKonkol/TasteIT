package pl.jakubkonkol.tasteitserver.factory;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import pl.jakubkonkol.tasteitserver.model.*;
import pl.jakubkonkol.tasteitserver.model.enums.PostType;

@Component
public class PostDrinkFactory extends PostFactory {

    @Override
    public Post createPost(JSONObject postObj) {
        Post newPost = new Post();
        newPost.setPostType(PostType.DRINK);

        PostMedia postMedia = createPostMedia(postObj, "strDrink", "strDrinkThumb", "Very nice drink, drop like!");
        newPost.setPostMedia(postMedia);

        Recipe recipe = createRecipe(postObj, "strInstructions");
        recipe.setIngredientsWithMeasurements(createIngredients(postObj));
        newPost.setRecipe(recipe);
        String strAlcoholic = postObj.getString("strAlcoholic");
        boolean isAlcoholic = strAlcoholic.equals("Alcoholic");
        newPost.setIsAlcoholic(isAlcoholic);

        var tags = createTags(postObj);
        newPost.setTags(tags);

        // TODO: Set proper user and tags, likes, comments, etc.
        newPost.setUserId("0"); //userId=0 -> id admina
        newPost.setLikes(null);
        newPost.setComments(null);

        return newPost;
    }
}
