package pl.jakubkonkol.tasteitserver.model.value;

import lombok.Value;
import pl.jakubkonkol.tasteitserver.model.Post;

@Value
public class ScoredPost {
    Post post;
    double score;
}
