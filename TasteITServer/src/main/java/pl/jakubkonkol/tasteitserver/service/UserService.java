package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.dto.*;
import pl.jakubkonkol.tasteitserver.model.Badge;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.model.projection.UserProfileView;
import pl.jakubkonkol.tasteitserver.model.projection.UserShort;
import pl.jakubkonkol.tasteitserver.repository.PostRepository;
import pl.jakubkonkol.tasteitserver.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PostRepository postRepository;

    public UserReturnDto getUserDtoById(String userId, String sessionToken) {
        User user = getUserById(userId);
        User currentUser = getCurrentUserBySessionToken(sessionToken);

        UserReturnDto userReturnDto = convertToDto(user);
        userReturnDto.setIsFollowing(currentUser.getFollowing().contains(userId));

        return userReturnDto;
    }

    //method for fetching user info on profile
    public UserReturnDto getUserProfileView(String userId, String sessionToken) {
        UserProfileView userProfileView = userRepository.findUserByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        User currentUser = getCurrentUserBySessionToken(sessionToken);
        return convertUserProfileViewToUserReturnDto(userId, userProfileView, currentUser);
    }

    public UserReturnDto getCurrentUserDtoBySessionToken(String sessionToken) {
        User user = getCurrentUserBySessionToken(sessionToken);
        return convertToDto(user);
    }// mozna pomyslesc o cache'owaniu w celu optymalizacji

    public void updateUserProfile(
        UserProfileDto userProfileDto
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

    public void changeUserFirstLogin(String userId) {
        checkIfUserExists(userId);
        userRepository.setFirstLoginToFalse(userId);
    }

    public void updateUserTags(String userId, UserTagsDto userTagsDto) {
        checkIfUserExists(userId);
        userRepository.updateUserTagsByUserId(userId, userTagsDto.getTags());
    }


    public void updateUserBadges(String userId, List<Badge> updatedBadges) { //TODO Zrobić prosty update na bazie save(pobrac zmienic zapisac) /////// JEDNAK TO DZIAŁAAAAAAAAA
        checkIfUserExists(userId);
        userRepository.updateUserBadgesByUserId(userId, updatedBadges);
    }


    public void followUser(String targetUserId, String sessionToken) {
        checkIfUserExists(targetUserId);
        User currentUser = getCurrentUserBySessionToken(sessionToken);

        if (currentUser.getUserId().equals(targetUserId)) {
            throw new IllegalArgumentException("User cannot follow themselves.");
        }

        if (!currentUser.getFollowing().contains(targetUserId)) {
            userRepository.addFollowing(currentUser.getUserId(), targetUserId);
            userRepository.addFollower(targetUserId, currentUser.getUserId());
        } else {
            throw new IllegalStateException("User is already following the target user.");
        }
    }

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

    public PageDto<UserReturnDto> getFollowers(String userId, String sessionToken, Integer page, Integer size) {
        checkIfUserExists(userId);
        User currentUser = getCurrentUserBySessionToken(sessionToken);
        List<String> followers = userRepository.findFollowersByUserId(userId)
                .map(User::getFollowers)
                .orElse(new ArrayList<>());

        return getUserPage(followers, currentUser, page, size);
    }

    public PageDto<UserReturnDto> getFollowing(String userId, String sessionToken, Integer page, Integer size) {
        checkIfUserExists(userId);
        User currentUser = getCurrentUserBySessionToken(sessionToken);
        List<String> following = userRepository.findFollowingByUserId(userId)
                .map(User::getFollowing)
                .orElse(new ArrayList<>());

        return getUserPage(following, currentUser, page, size);
    }

    private PageDto<UserReturnDto> getUserPage(List<String> userIds, User currentUser, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserShort> userPage = userRepository.findUsersByUserIdIn(userIds, pageable);
        return getUserReturnDtoPageDto(page, size, currentUser, userPage);
    }

    public PageDto<UserReturnDto> searchUsersByDisplayName(String query, String sessionToken, Integer page, Integer size) {
        User currentUser = getCurrentUserBySessionToken(sessionToken);
        Pageable pageable = PageRequest.of(page, size);
        Page<UserShort> userPage = userRepository.findByDisplayNameContainingIgnoreCase(query, pageable);

        return getUserReturnDtoPageDto(page, size, currentUser, userPage);
    }

    private PageDto<UserReturnDto> getUserReturnDtoPageDto(Integer page, Integer size, User currentUser, Page<UserShort> userPage) {
        List<UserReturnDto> userDtos = userPage.getContent().stream().map(user -> {
            UserReturnDto userReturnDto = new UserReturnDto();
            userReturnDto.setUserId(user.getUserId());
            userReturnDto.setDisplayName(user.getDisplayName());
            userReturnDto.setProfilePicture(user.getProfilePicture());
            userReturnDto.setIsFollowing(currentUser.getFollowing().contains(user.getUserId()));

            return userReturnDto;
        }).toList();

        return getUsersShortPageDto(page, size, userPage, userDtos);
    }

    private PageDto<UserReturnDto> getUsersShortPageDto(Integer page, Integer size, Page<UserShort> userPage, List<UserReturnDto> userDtos) {
        PageDto<UserReturnDto> pageDto = new PageDto<>();
        pageDto.setContent(userDtos);
        pageDto.setPageNumber(page);
        pageDto.setPageSize(size);
        pageDto.setTotalElements(userPage.getTotalElements());
        pageDto.setTotalPages(userPage.getTotalPages());

        return pageDto;
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NoSuchElementException("User with id " + userId + " not found"));
    }

    public User getCurrentUserBySessionToken(String sessionToken) {
        return userRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    private UserReturnDto convertToDto(User user) {
        UserReturnDto userReturnDto = modelMapper.map(user, UserReturnDto.class);
        userReturnDto.setFollowersCount((long) user.getFollowers().size());
        userReturnDto.setFollowingCount((long) user.getFollowing().size());
        userReturnDto.setBadges(user.getBadges());
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
}

