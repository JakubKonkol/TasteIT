package pl.jakubkonkol.tasteitserver.dto;

import lombok.Data;
import pl.jakubkonkol.tasteitserver.model.Badge;
import pl.jakubkonkol.tasteitserver.model.Tag;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Data
public class UserReturnDto {
    private String userId;
    private String email;
    private String displayName;
    private String bio;
    private String profilePicture;
    private Date createdAt;
    private Date birthDate;
    private Boolean firstLogin;
    private List<String> roles;
    private List<Tag> tags;
    private Long followersCount;
    private Long followingCount;
    private Boolean isFollowing;
    private Long postsCount;
    private List<Badge> badges = new ArrayList<>();
//    private List<BadgeBlueprint> badgeBlueprints = new ArrayList<>(); //tutaj bage ze statusem te nowe, nie blueprint
}
