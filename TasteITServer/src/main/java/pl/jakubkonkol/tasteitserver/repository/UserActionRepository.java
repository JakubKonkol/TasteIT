package pl.jakubkonkol.tasteitserver.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pl.jakubkonkol.tasteitserver.model.UserAction;

import java.time.LocalDateTime;
import java.util.List;

public interface UserActionRepository extends MongoRepository<UserAction, String> {
    @Query(value = "{ 'userId': ?0, 'actionType': { $in: ['LIKE_POST', 'COMMENT_POST'] }}",
            fields = "{ 'metadata.postId': 1 }")
    List<UserAction> findInteractionsByUserId(String userId);

    List<UserAction> findByTimestampAfter(LocalDateTime thirtyDaysAgo);
}
