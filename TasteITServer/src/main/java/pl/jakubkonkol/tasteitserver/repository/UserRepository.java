package pl.jakubkonkol.tasteitserver.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pl.jakubkonkol.tasteitserver.model.FoodList;
import pl.jakubkonkol.tasteitserver.model.User;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String>{
    Optional<User> findByEmail(String email);
    @Query("{ 'authentication.sessionToken' : ?0 }")
    Optional<User> findBySessionToken(String sessionToken);
}
