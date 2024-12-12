package pl.jakubkonkol.tasteitserver.service.interfaces;

import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.model.value.ScoredPost;

import java.util.List;

public interface IFeedScoringService {
    List<ScoredPost> calculateScores(List<Post> posts, User currentUser);
}
