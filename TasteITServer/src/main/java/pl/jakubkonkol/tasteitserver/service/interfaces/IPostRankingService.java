package pl.jakubkonkol.tasteitserver.service.interfaces;

import pl.jakubkonkol.tasteitserver.model.Post;

import java.util.List;

public interface IPostRankingService {
    List<Post> getRankedPostsForUser(String userId);
    void clearRankedPostsCacheForUser(String userId);
}
