package pl.jakubkonkol.tasteitserver.model.value;

import lombok.Value;
import pl.jakubkonkol.tasteitserver.model.Post;

public record ScoredPost(Post post, double score) {
}
