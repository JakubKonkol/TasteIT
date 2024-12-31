package pl.jakubkonkol.tasteitserver.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import pl.jakubkonkol.tasteitserver.model.Notification;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    long countByUserIdAndReadFalse(String userId);

    List<Notification> findByUserIdAndReadFalse(String userId);

    long deleteByReadTrueAndCreatedAtBefore(LocalDateTime thirtyDaysAgo);
}
