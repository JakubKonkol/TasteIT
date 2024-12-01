package pl.jakubkonkol.tasteitserver.service;

import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class PostScoringService {
    public double calculatePostScore(Post post, User user) {
        double score = calculateTimeScore(post.getCreatedDate());
        score += post.getNumberOfComments() * 3;
        score += post.getNumberOfLikes();

// Dopasowanie tagów z tags użytkownika
        boolean isTagMatch = user.getTags().stream()
                .anyMatch(userTag -> post.getTags().stream()
                        .anyMatch(postTag -> postTag.getTagId().equals(userTag.getTagId())));
        if (isTagMatch) score += 10;

        // Sprawdzenie, czy użytkownik obserwuje autora
        if (user.getFollowing().contains(post.getUserId())) {
            score *= 1.2;
        }

        // Podbicie punktów dla postów administratora TasteIT
        if ("TasteITAdmin".equals(post.getUserId())) {
            score *= 100;
        }

        return score;
    }

    private double calculateTimeScore(Date createdDate) {
        long hoursSinceCreation = ChronoUnit.HOURS.between(createdDate.toInstant(), Instant.now());
        if (hoursSinceCreation <= 6) return 20 + 2.0 * hoursSinceCreation;
        if (hoursSinceCreation <= 12) return 20 + 1.5 * hoursSinceCreation;
        if (hoursSinceCreation <= 24) return 20 + 1.0 * hoursSinceCreation;
        return 10;
    }
}
