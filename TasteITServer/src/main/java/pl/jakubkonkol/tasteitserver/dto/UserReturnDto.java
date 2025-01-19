package pl.jakubkonkol.tasteitserver.dto;

import lombok.Data;
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
    private Long followersCount;
    private Long followingCount;
    private Boolean isFollowing;
    private Long postsCount;
}
