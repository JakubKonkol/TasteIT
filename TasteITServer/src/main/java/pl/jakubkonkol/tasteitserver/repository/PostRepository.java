package pl.jakubkonkol.tasteitserver.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.jakubkonkol.tasteitserver.model.Like;
import pl.jakubkonkol.tasteitserver.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pl.jakubkonkol.tasteitserver.model.enums.PostType;
import pl.jakubkonkol.tasteitserver.model.projection.PostPhotoView;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends MongoRepository<Post, String> {
    Page<PostPhotoView> findPostsByTagsTagId(String tagId, Pageable pageable);

    List<Post> findByLikesIn(List<Like> likes);

    @Query("{ 'likes': { $exists: true, $ne: [] } }")
    List<Post> findByLikesNotEmpty();

    @Query("{ 'comments': { $exists: true, $ne: [] } }")
    List<Post> findByCommentsNotEmpty();
    Optional<Post> findById(String id);
    Long countByUserId(String userId);

    Page<PostPhotoView> findPostsByUserId(String userId, Pageable pageable);

    @Query("{ 'recipe.ingredientsWithMeasurements.name': { $nin: ?0 } }")
    Page<PostPhotoView> findByExcludedIngredients(List<String> ingredientNames, Pageable pageable);

    // Znajdź posty zawierające co najmniej jeden z podanych składników
    @Query("{ 'recipe.ingredientsWithMeasurements.name': { $in: ?0 } }")
    Page<PostPhotoView> findByAnyIngredientInRecipe(List<String> ingredientNames, Pageable pageable);

    @Query("{ 'recipe.ingredientsWithMeasurements.name': { $not: { $elemMatch: { $nin: ?0 } } } }")
    Page<PostPhotoView> findByIngredientsSubset(List<String> ingredientNames, Pageable pageable);

}
