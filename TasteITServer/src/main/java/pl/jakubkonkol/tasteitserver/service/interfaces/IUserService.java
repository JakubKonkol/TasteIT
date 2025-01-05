package pl.jakubkonkol.tasteitserver.service.interfaces;

import pl.jakubkonkol.tasteitserver.dto.*;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.model.projection.UserShort;

import java.util.List;

public interface IUserService {
    UserReturnDto getUserDtoById(String userId, String sessionToken);
    UserReturnDto getUserProfileView(String userId, String sessionToken);
    UserReturnDto getCurrentUserDtoBySessionToken(String sessionToken);
    void updateUserProfile(UserProfileDto userProfileDto, String sessionToken);
    void changeUserFirstLogin(String userId, String sessionToken);
    void updateUserTags(String userId, UserTagsDto userTagsDto, String sessionToken);
    void followUser(String targetUserId, String sessionToken);
    void unfollowUser(String targetUserId, String sessionToken);
    PageDto<UserReturnDto> getFollowers(String userId, String sessionToken, Integer page, Integer size);
    PageDto<UserReturnDto> getFollowing(String userId, String sessionToken, Integer page, Integer size);
    PageDto<UserReturnDto> searchUsersByDisplayName(String query, String sessionToken, Integer page, Integer size);
    User getUserById(String userId);
    User getCurrentUserBySessionToken(String sessionToken);
    void checkIfUserExists(String userId);
    User saveUser(User user);
    List<UserShort> getUserShortByIdIn(List<String> userIds);
    UserShort findUserShortByUserId(String userId);
    String getCurrentUserId();
    void updateUserBannedIngredients(String sessionToken, List<IngredientDto> ingredients);
    void updateUserBannedTags(String sessionToken, List<TagDto> tags);
    List<User> findUsersActiveInLast30Days();
}
