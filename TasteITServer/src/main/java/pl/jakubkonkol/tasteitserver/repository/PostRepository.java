package pl.jakubkonkol.tasteitserver.repository;

import pl.jakubkonkol.tasteitserver.model.Like;
import pl.jakubkonkol.tasteitserver.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findByPostMediaTitleContainingIgnoreCase(String title);
    List<Post> findByLikesIn(List<Like> likes);
    List<Post> findByLikesNotEmpty();
    List<Post> findByCommentsNotEmpty();
}
