package pl.jakubkonkol.tasteitserver.repository;

import pl.jakubkonkol.tasteitserver.model.Like;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends MongoRepository<Like, String> {
    Optional<Like> findByPostIdAndUserId(String postId, String userId);
    List<Like> findByUserId(String userId);

    void deleteAllByUserId(String userId);
}
