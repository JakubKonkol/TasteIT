package pl.jakubkonkol.tasteitserver.repository;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import pl.jakubkonkol.tasteitserver.model.Like;
import pl.jakubkonkol.tasteitserver.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pl.jakubkonkol.tasteitserver.model.enums.PostType;
import pl.jakubkonkol.tasteitserver.model.projection.PostPhotoView;

import java.util.Date;
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

    @Query("{ 'recipe.ingredientsWithMeasurements.name': { $in: ?0 } }")
    List<Post> findByAnyIngredientInRecipe(List<String> ingredientNames);

    @Query("{ 'recipe.ingredientsWithMeasurements.name': { $not: { $elemMatch: { $nin: ?0 } } } }")
    List<Post> findByIngredientsSubset(List<String> ingredientNames);

    List<Post> findAll();

    List<Post> findTop100ByOrderByCreatedDateDesc();

    @Query("{ 'clusters.$id': ?0, 'createdDate': { $gt: ?1 } }")
    List<Post> findByClustersAndCreatedDateAfter(
            ObjectId clusterId,
            Date cutoffDate,
            Pageable pageable
    );

    @Query("{ 'createdDate': { $gt: ?0 }, 'postId': { $nin: ?1 } }")
    List<Post> findByCreatedDateAfterAndPostIdNotIn(
            Date cutoffDate,
            List<String> excludePostIds,
            Pageable pageable
    );

    @Query("{ 'userId': { $in: ?0 }, 'createdDate': { $gt: ?1 }, 'postId': { $nin: ?2 } }")
    List<Post> findByUserIdInAndCreatedDateAfterAndPostIdNotIn(
            List<String> userIds,
            Date cutoffDate,
            List<String> excludePostIds,
            Pageable pageable
    );

    @Query(value = "{ 'clusters.$id': ?0, 'createdDate': { $gt: ?1 } }", count = true)
    long countByClustersAndCreatedDateAfter(ObjectId clusterId, Date cutoffDate);
}
