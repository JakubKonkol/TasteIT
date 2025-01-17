package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import pl.jakubkonkol.tasteitserver.dto.*;
import pl.jakubkonkol.tasteitserver.model.Ingredient;
import pl.jakubkonkol.tasteitserver.model.Tag;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.model.UserAction;
import pl.jakubkonkol.tasteitserver.model.enums.NotificationType;
import pl.jakubkonkol.tasteitserver.model.projection.PostPhotoView;
import pl.jakubkonkol.tasteitserver.model.projection.UserProfileView;
import pl.jakubkonkol.tasteitserver.model.projection.UserShort;
import pl.jakubkonkol.tasteitserver.repository.PostRepository;
import pl.jakubkonkol.tasteitserver.repository.UserActionRepository;
import pl.jakubkonkol.tasteitserver.repository.UserRepository;
import pl.jakubkonkol.tasteitserver.service.interfaces.IPostService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IUserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PostRepository postRepository;
    private final IngredientService ingredientService;
    private final TagService tagService;
    private final UserActionRepository userActionRepository;

    private PostService postService;
    private final NotificationEventPublisher notificationEventPublisher;
    private static final java.util.logging.Logger LOGGER = Logger.getLogger(UserService.class.getName());

    @Cacheable(value = "userById", key = "#userId")
    public UserReturnDto getUserDtoById(String userId, String sessionToken) {
        User user = getUserById(userId);
        User currentUser = getCurrentUserBySessionToken(sessionToken);

        UserReturnDto userReturnDto = convertToDto(user);
        userReturnDto.setIsFollowing(currentUser.getFollowing().contains(userId));

        return userReturnDto;
    }

    public UserReturnDto getUserProfileView(String userId, String sessionToken) {
        UserProfileView userProfileView = userRepository.findUserByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        User currentUser = getCurrentUserBySessionToken(sessionToken);
        return convertUserProfileViewToUserReturnDto(userId, userProfileView, currentUser);
    }

    @Cacheable(value = "userBySessionToken", key = "#sessionToken")
    public UserReturnDto getCurrentUserDtoBySessionToken(String sessionToken) {
        User user = getCurrentUserBySessionToken(sessionToken);
        return convertToDto(user);
    }

    @Caching(evict = {
        @CacheEvict(value = {"userById", "userProfileView", "userShort"}, key = "#userProfileDto.userId"),
        @CacheEvict(value = "userBySessionToken", key = "#sessionToken")
    })
    public void updateUserProfile(
        UserProfileDto userProfileDto,
        String sessionToken
    ) {
        checkIfUserExists(userProfileDto.getUserId());
        userRepository.updateUserProfileFields(
                userProfileDto.getUserId(),
                userProfileDto.getDisplayName(),
                userProfileDto.getBio(),
                userProfileDto.getProfilePicture(),
                userProfileDto.getBirthDate()
        );
    }

    @Caching(evict = {
        @CacheEvict(value = {"userById", "userProfileView", "userShort"}, key = "#userId"),
        @CacheEvict(value = "userBySessionToken", key = "#sessionToken")
    })
    public void changeUserFirstLogin(String userId, String sessionToken) {
        checkIfUserExists(userId);
        userRepository.setFirstLoginToFalse(userId);
    }

    @Caching(evict = {
        @CacheEvict(value = {"userById", "userProfileView", "userShort"}, key = "#userId"),
        @CacheEvict(value = "userBySessionToken", key = "#sessionToken")
    })
    public void updateUserTags(String userId, UserTagsDto userTagsDto, String sessionToken) {
        User user = getUserById(userId);
        user.setTags(userTagsDto.getTags());
        userRepository.save(user);
    }

    @CacheEvict(value = {"followers", "following"}, allEntries = true)
    public void followUser(String targetUserId, String sessionToken) {
        checkIfUserExists(targetUserId);
        User currentUser = getCurrentUserBySessionToken(sessionToken);

        if (currentUser.getUserId().equals(targetUserId)) {
            throw new IllegalArgumentException("User cannot follow themselves.");
        }

        if (!currentUser.getFollowing().contains(targetUserId)) {
            userRepository.addFollowing(currentUser.getUserId(), targetUserId);
            userRepository.addFollower(targetUserId, currentUser.getUserId());
            handleFollowNotification(currentUser.getUserId(), targetUserId);
        } else {
            throw new IllegalStateException("User is already following the target user.");
        }
    }

    @CacheEvict(value = {"followers", "following"}, allEntries = true)
    public void unfollowUser(String targetUserId, String sessionToken) {
        checkIfUserExists(targetUserId);
        User currentUser = getCurrentUserBySessionToken(sessionToken);

        if (currentUser.getUserId().equals(targetUserId)) {
            throw new IllegalArgumentException("User cannot unfollow themselves.");
        }

        if (currentUser.getFollowing().contains(targetUserId)) {
            userRepository.removeFollowing(currentUser.getUserId(), targetUserId);
            userRepository.removeFollower(targetUserId, currentUser.getUserId());
        } else {
            throw new IllegalStateException("User is not following the target user.");
        }
    }

    @Cacheable(value = "followers", key = "#userId")
    public PageDto<UserReturnDto> getFollowers(String userId, String sessionToken, Integer page, Integer size) {
        checkIfUserExists(userId);
        User currentUser = getCurrentUserBySessionToken(sessionToken);
        List<String> followers = userRepository.findFollowersByUserId(userId)
                .map(User::getFollowers)
                .orElse(new ArrayList<>());

        return getUserPage(followers, currentUser, page, size);
    }

    @Cacheable(value = "following", key = "#userId")
    public PageDto<UserReturnDto> getFollowing(String userId, String sessionToken, Integer page, Integer size) {
        checkIfUserExists(userId);
        User currentUser = getCurrentUserBySessionToken(sessionToken);
        List<String> following = userRepository.findFollowingByUserId(userId)
                .map(User::getFollowing)
                .orElse(new ArrayList<>());

        return getUserPage(following, currentUser, page, size);
    }

    // is this needed?
    private PageDto<UserReturnDto> getUserPage(List<String> userIds, User currentUser, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserShort> userPage = userRepository.findUsersByUserIdIn(userIds, pageable);
        return getUserReturnDtoPageDto(currentUser, userPage, pageable);
    }

    public PageDto<UserReturnDto> searchUsersByDisplayName(String query, String sessionToken, Integer page, Integer size) {
        User currentUser = getCurrentUserBySessionToken(sessionToken);
        Pageable pageable = PageRequest.of(page, size);
        Page<UserShort> userPage = userRepository.findByDisplayNameContainingIgnoreCase(query, pageable);

        return getUserReturnDtoPageDto(currentUser, userPage, pageable);
    }

    private PageDto<UserReturnDto> getUserReturnDtoPageDto(User currentUser, Page<UserShort> userPage, Pageable pageable) {
        List<UserReturnDto> userDtos = userPage.getContent().stream().map(user -> {
            UserReturnDto userReturnDto = new UserReturnDto();
            userReturnDto.setUserId(user.getUserId());
            userReturnDto.setDisplayName(user.getDisplayName());
            userReturnDto.setProfilePicture(user.getProfilePicture());
            userReturnDto.setIsFollowing(currentUser.getFollowing().contains(user.getUserId()));

            return userReturnDto;
        }).toList();

        PageImpl<UserReturnDto> pageImpl = new PageImpl<>(userDtos, pageable, userPage.getTotalElements());
        return getUsersShortPageDto(pageImpl);
    }

    private PageDto<UserReturnDto> getUsersShortPageDto(PageImpl<UserReturnDto> pageImpl) {
        PageDto<UserReturnDto> pageDto = new PageDto<>();
        pageDto.setContent(pageImpl.getContent());
        pageDto.setPageNumber(pageImpl.getNumber());
        pageDto.setPageSize(pageImpl.getSize());
        pageDto.setTotalElements(pageImpl.getTotalElements());
        pageDto.setTotalPages(pageImpl.getTotalPages());

        return pageDto;
    }

    //@Cacheable(value = "userById", key = "#userId")
    public User getUserById(String userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NoSuchElementException("User with id " + userId + " not found"));
    }

    public User getCurrentUserBySessionToken(String sessionToken) {
        return userRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    public UserShort getCurrentUserShortBySessionToken(String sessionToken) {
        return userRepository.findUserShortBySessionToken(sessionToken)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    private UserReturnDto convertToDto(User user) {
        UserReturnDto userReturnDto = modelMapper.map(user, UserReturnDto.class);
        userReturnDto.setFollowersCount((long) user.getFollowers().size());
        userReturnDto.setFollowingCount((long) user.getFollowing().size());

        return userReturnDto;
    }

    private UserProfileDto convertToProfileDto(User user) {
        return modelMapper.map(user, UserProfileDto.class);
    }

    private User convertProfileToEntity(UserProfileDto dto) {
        return modelMapper.map(dto, User.class);
    }

    public void checkIfUserExists(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("User with id " + userId + " does not exist.");
        }
    }

    @CacheEvict(value = {"userShort", "userById", "userBySessionToken", "userShort"}, allEntries = true)
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    private UserReturnDto convertUserProfileViewToUserReturnDto(String userId, UserProfileView userProfileView, User currentUser) {
        UserReturnDto userReturnDto = new UserReturnDto();
        userReturnDto.setUserId(userProfileView.getUserId());
        userReturnDto.setEmail(userProfileView.getEmail());
        userReturnDto.setDisplayName(userProfileView.getDisplayName());
        userReturnDto.setBio(userProfileView.getBio());
        userReturnDto.setProfilePicture(userProfileView.getProfilePicture());
        userReturnDto.setCreatedAt(userProfileView.getCreatedAt());
        userReturnDto.setBirthDate(userProfileView.getBirthDate());
        userReturnDto.setFirstLogin(userProfileView.getFirstLogin());
        userReturnDto.setRoles(userProfileView.getRoles());
        userReturnDto.setFollowersCount((long) userProfileView.getFollowers().size());
        userReturnDto.setFollowingCount((long) userProfileView.getFollowing().size());
        Long postsCount = postRepository.countByUserId(userId);
        userReturnDto.setPostsCount(postsCount);
        userReturnDto.setIsFollowing(currentUser.getFollowing().contains(userId));

        return userReturnDto;
    }

    public List<UserShort> getUserShortByIdIn(List<String> userIds) {
        return userRepository.findUsersByUserIdIn(userIds);
    }


//    @Cacheable(value = "userShort", key = "#userId")
    public UserShort findUserShortByUserId(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return userRepository.findUserShortByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
    }

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return (String) authentication.getDetails();
        }
        return null;
    }

    public void updateUserBannedIngredients(String sessionToken, List<IngredientDto> ingredients) {
        User user = getCurrentUserBySessionToken(sessionToken);
        List<Ingredient> bannedIngredients = ingredients.stream()
                .map(ingredientService::convertToEntity)
                .toList();
        user.setBannedIngredients(bannedIngredients);
        userRepository.save(user);
    }

    public void updateUserBannedTags(String sessionToken, List<TagDto> tags) {
        User user = getCurrentUserBySessionToken(sessionToken);
        List<Tag> bannedTags = tags.stream()
                .map(tagService::convertToEntity)
                .toList();
        user.setBannedTags(bannedTags);
        userRepository.save(user);
    }

    public List<User> findUsersActiveInLast30Days() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<String> activeUserIds = userActionRepository
                .findByTimestampAfter(thirtyDaysAgo)
                .stream()
                .map(UserAction::getUserId)
                .distinct()
                .toList();

        return userRepository.findAllById(activeUserIds);
    }

    @CacheEvict(value = {"users", "userById"}, allEntries = true)
    public void deleteUser(String email, String sessionToken) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User with email " + email + " not found"));
        
        // Delete all user posts
        if (user.getPosts() != null) {
            user.getPosts().forEach(post -> {
                try {
                    postService.deletePost(post.getPostId(), sessionToken);
                } catch (Exception e) {
                    // Log error but continue with deletion
                    System.err.println("Error deleting post " + post.getPostId() + ": " + e.getMessage());
                }
            });
        }
        
        // Delete user
        userRepository.delete(user);
    }

    private void handleFollowNotification(String followerId, String targetUserId) {
        try {
            notificationEventPublisher.publishNotification(
                    NotificationType.NEW_FOLLOWER,
                    targetUserId,
                    followerId,
                    null
            );
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,"Failed to send follow notification", e);
        }
    }
}

