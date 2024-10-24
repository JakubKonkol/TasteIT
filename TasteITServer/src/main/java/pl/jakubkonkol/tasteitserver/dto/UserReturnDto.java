package pl.jakubkonkol.tasteitserver.dto;

import lombok.Data;
import pl.jakubkonkol.tasteitserver.model.Authentication;
import pl.jakubkonkol.tasteitserver.model.Tag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Data
public class UserReturnDto {
    private String userId;
    private String email;
    private String displayName;
    private String bio;
    private String profilePicture;
    private LocalDate createdAt;
    private LocalDate birthDate;
    private Boolean firstLogin;
    private List<String> roles;
    private List<Tag> mainTags;
    private List<Tag> customTags;
    private Long followersCount;
    private Long followingCount;
    private Boolean isFollowing;
    private Integer postsCount;
}
