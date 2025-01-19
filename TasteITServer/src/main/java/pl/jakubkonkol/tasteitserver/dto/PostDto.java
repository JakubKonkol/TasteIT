package pl.jakubkonkol.tasteitserver.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
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
    private PostAuthorDto postAuthorDto;
    @NotNull(message = "Type of the Post cannot be null")
    private PostType postType;
    @NotNull(message = "Post Media cannot be null")
    @Valid
    private PostMedia postMedia;
    @NotNull(message = "Recipe cannot be null")
    @Valid
    private Recipe recipe;
    private Boolean isAlcoholic;
    private List<Tag> tags = new ArrayList<>();
    @PastOrPresent(message = "Created date cannot be in the future")
    private Date createdDate;
    private Long likesCount;
    private Long commentsCount;
    private Boolean likedByCurrentUser;
}
