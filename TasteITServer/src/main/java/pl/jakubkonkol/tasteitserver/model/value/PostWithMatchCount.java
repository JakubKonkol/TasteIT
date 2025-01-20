package pl.jakubkonkol.tasteitserver.model.value;

import pl.jakubkonkol.tasteitserver.model.Post;

public record PostWithMatchCount(Post post, int matchCount) {
}