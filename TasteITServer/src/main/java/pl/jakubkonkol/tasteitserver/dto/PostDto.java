package pl.jakubkonkol.tasteitserver.dto;

import lombok.Data;
import pl.jakubkonkol.tasteitserver.model.PostMedia;
import pl.jakubkonkol.tasteitserver.model.enums.PostType;

import java.util.Date;

@Data
public class PostDto {
    private String postId;
    private String userId;
    private PostType postType;
    private PostMedia postMedia;
    private Date createdDate;
    private Long likesCount;
    private Long commentsCount;
    private Boolean likedByCurrentUser;
}
