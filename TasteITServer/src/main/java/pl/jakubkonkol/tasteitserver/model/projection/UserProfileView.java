package pl.jakubkonkol.tasteitserver.model.projection;

import java.util.Date;
import java.util.List;

public interface UserProfileView {
    String getUserId();
    String getEmail();
    String getDisplayName();
    String getBio();
    String getProfilePicture();
    Date getCreatedAt();
    Date getBirthDate();
    Boolean getFirstLogin();
    List<String> getRoles();
    List<String> getFollowers();
    List<String> getFollowing();
}
