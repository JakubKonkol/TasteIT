package pl.jakubkonkol.tasteitserver.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pl.jakubkonkol.tasteitserver.model.FoodList;
import pl.jakubkonkol.tasteitserver.model.Like;
import pl.jakubkonkol.tasteitserver.model.Post;

import java.util.List;

public interface FoodListRepository extends MongoRepository<FoodList, String> {
    FoodList findByFoodListId(String foodListId);
    List<FoodList> findAllByUserId(String userId);
}
