package pl.jakubkonkol.tasteitserver.service.interfaces;

import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.User;

import java.util.List;

public interface IContentFilterService {
    List<Post> filterBannedContent(List<Post> posts, User user);
}
