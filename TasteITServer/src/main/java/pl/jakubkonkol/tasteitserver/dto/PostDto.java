package pl.jakubkonkol.tasteitserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pl.jakubkonkol.tasteitserver.model.PostMedia;
import pl.jakubkonkol.tasteitserver.model.Recipe;
import pl.jakubkonkol.tasteitserver.model.Tag;
import pl.jakubkonkol.tasteitserver.model.enums.PostType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class PostDto {
    private String postId;
    @NotNull(message = "UserId cannot be null.")
    private PostAuthorDto postAuthorDto;
    @NotNull(message = "PostType cannot be null.")
    private PostType postType;
    private PostMedia postMedia;
    private Recipe recipe;
    private Boolean isAlcoholic;
    private List<Tag> tags = new ArrayList<>();
    private Date createdDate;
    private Long likesCount;
    private Long commentsCount;
    private Boolean likedByCurrentUser;
}
