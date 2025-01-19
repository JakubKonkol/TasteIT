package pl.jakubkonkol.tasteitserver.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.model.projection.UserProfileView;
import pl.jakubkonkol.tasteitserver.model.projection.UserShort;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String>{
    Optional<User> findByEmail(String email);

    @Query("{ 'authentication.sessionToken' : ?0 }")
    Optional<User> findBySessionToken(String sessionToken);

    @Query("{ 'authentication.sessionToken' : ?0 }")
    Optional<UserShort> findUserShortBySessionToken(String sessionToken);

    @Query(value = "{ 'userId': { $in: ?0 } }")
    Page<UserShort> findUsersByUserIdIn(List<String> userIds, Pageable pageable);

    @Query(value = "{ 'userId': { $in: ?0 } }")
    List<UserShort> findUsersByUserIdIn(List<String> userIds);

    Page<UserShort> findByDisplayNameContainingIgnoreCase(String displayName, Pageable pageable);

    @Query(value = "{ 'userId': ?0 }", fields = "{ 'followers' : 1 }")
    Optional<User> findFollowersByUserId(String userId);

    @Query(value = "{ 'userId': ?0 }", fields = "{ 'following' : 1 }")
    Optional<User> findFollowingByUserId(String userId);

    @Query("{'userId' : ?0}")
    @Update("{ '$addToSet' : { 'followers' : ?1 } }")
    void addFollower(String userId, String followerId);

    @Query("{'userId' : ?0}")
    @Update("{ '$addToSet' : { 'following' : ?1 } }")
    void addFollowing(String userId, String followingId);

    @Query("{'userId' : ?0}")
    @Update("{ '$pull' : { 'followers' : ?1 } }")
    void removeFollower(String userId, String followerId);

    @Query("{'userId' : ?0}")
    @Update("{ '$pull' : { 'following' : ?1 } }")
    void removeFollowing(String userId, String followingId);

    @Query("{'userId' : ?0}")
    @Update("{ '$set' : { 'firstLogin' : false } }")
    void setFirstLoginToFalse(String userId);

    Optional<UserProfileView> findUserByUserId(String userId);

    Optional<UserShort> findUserShortByUserId(String userId);

    @Query("{ 'userId' : ?0 }")
    @Update("{ '$set' : { 'displayName' : ?1, 'bio' : ?2, 'profilePicture' : ?3, 'birthDate' : ?4 } }")
    void updateUserProfileFields(String userId, String displayName, String bio, String profilePicture, Date birthDate);

    @Query(value = "{ '_id' : ?0 }", fields = "{ 'clusterPreferences' : 1}")
    Optional<User> findClusterPreferencesById(String userId);

    void deleteByEmail(String email);

    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'following': [] } }")
    void clearFollowingList(String userId);

    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'followers': [] } }")
    void clearFollowersList(String userId);
}
