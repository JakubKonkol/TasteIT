package pl.jakubkonkol.tasteitserver.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.jakubkonkol.tasteitserver.model.Like;
import pl.jakubkonkol.tasteitserver.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pl.jakubkonkol.tasteitserver.model.enums.PostType;

import java.util.List;

public interface PostRepository extends MongoRepository<Post, String> {
    @Query("{ 'tags.tagName': { $regex: '^?0$', $options: 'i' } }")
    Page<Post> findByTagNameIgnoreCase(String tagName, Pageable pageable);
    List<Post> findByLikesIn(List<Like> likes);
    @Query("{ 'likes': { $exists: true, $ne: [] } }")
    List<Post> findByLikesNotEmpty();
    @Query("{ 'comments': { $exists: true, $ne: [] } }")
    List<Post> findByCommentsNotEmpty();

//    List<Post> findPostsByUserId(String userId);


    Integer countByUserId(String userId);
}
