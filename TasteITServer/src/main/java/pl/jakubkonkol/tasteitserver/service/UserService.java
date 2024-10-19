package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.dto.*;
import pl.jakubkonkol.tasteitserver.model.Ingredient;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.repository.PostRepository;
import pl.jakubkonkol.tasteitserver.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public UserReturnDto getUserDtoById(String userId, String sessionToken) {
        User user = getUserById(userId);
        User currentUser = getCurrentUserBySessionToken(sessionToken);

        UserReturnDto userReturnDto = convertToDto(user);
        userReturnDto.setIsFollowing(currentUser.getFollowing().contains(userId));

        return userReturnDto;
    }

    public UserReturnDto getCurrentUserDtoBySessionToken(String sessionToken) {
        User user = getCurrentUserBySessionToken(sessionToken);
        return convertToDto(user);
    }// mozna pomyslesc o cache'owaniu w celu optymalizacji

    public UserReturnDto updateUserProfile(
            String userId,
            String newDisplayName,
            String newBio,
            String newProfilePicture,
            LocalDate newBirthDate
    ) {
        User user = getUserById(userId);
        user.setDisplayName(newDisplayName);
        user.setBio(newBio);
        user.setProfilePicture(newProfilePicture);
        user.setBirthDate(newBirthDate);

        userRepository.save(user);
        return convertToDto(user);
    }

    public UserReturnDto changeUserFirstLogin(String userId) {
        User user = getUserById(userId);
        user.setFirstLogin(false);

        userRepository.save(user);
        return convertToDto(user);
    }

    public UserReturnDto updateUserTags(String userId, UserTagsDto userTagsDto) {
        User user = getUserById(userId);
        user.setMainTags(userTagsDto.getMainTags());
        user.setCustomTags(userTagsDto.getCustomTags());

        userRepository.save(user);
        return convertToDto(user);
    }

    public void followUser(String targetUserId, String sessionToken) {
        User targetUser = getUserById(targetUserId);
        User currentUser = getCurrentUserBySessionToken(sessionToken);

        if (currentUser.getUserId().equals(targetUserId)) {
            throw new IllegalArgumentException("User cannot follow themselves.");
        }

        if (!currentUser.getFollowing().contains(targetUserId)) {
            currentUser.getFollowing().add(targetUserId);
            targetUser.getFollowers().add(currentUser.getUserId());
            userRepository.save(currentUser);
            userRepository.save(targetUser);
        } else {
            throw new IllegalStateException("User is already following the target user.");
        }
    }

    public void unfollowUser(String targetUserId, String sessionToken) {
        User targetUser = getUserById(targetUserId);
        User currentUser = getCurrentUserBySessionToken(sessionToken);


        if (currentUser.getUserId().equals(targetUserId)) {
            throw new IllegalArgumentException("User cannot unfollow themselves.");
        }
      
        if (currentUser.getFollowing().contains(targetUserId)) {
            currentUser.getFollowing().remove(targetUserId);
            targetUser.getFollowers().remove(currentUser.getUserId());
            userRepository.save(currentUser);
            userRepository.save(targetUser);
        } else {
            throw new IllegalStateException("User is not following the target user.");
        }
    }

    private User getUserById(String userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NoSuchElementException("User with id " + userId + " not found"));
    }

    private User getCurrentUserBySessionToken(String sessionToken) {
        return userRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    private UserReturnDto convertToDto(User user) {
        UserReturnDto userReturnDto = modelMapper.map(user, UserReturnDto.class);
        userReturnDto.setFollowersCount((long) user.getFollowers().size());
        userReturnDto.setFollowingCount((long) user.getFollowing().size());

        return userReturnDto;
    }
}

