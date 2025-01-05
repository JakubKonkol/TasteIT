package pl.jakubkonkol.tasteitserver.model;

import pl.jakubkonkol.tasteitserver.model.enums.PostType;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document
@Data
public class Post {
    @Id
    private String postId;
    private String userId;
    private PostType postType;
    private PostMedia postMedia;
    private Recipe recipe;
    private Boolean isAlcoholic;
    @DBRef
    private List<Tag> tags = new ArrayList<>();
    @DBRef
    private List<Like> likes = new ArrayList<>();
    @DBRef
    private List<Comment> comments = new ArrayList<>();
    @DBRef
    private List<Cluster> clusters = new ArrayList<>();

    @CreatedDate
    private Date createdDate;
}
