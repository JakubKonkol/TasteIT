package pl.jakubkonkol.tasteitserver.service.interfaces;

import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.User;

import java.util.List;

public interface IPostCandidatesService {
    List<Post> collectPosts(User user);
}
