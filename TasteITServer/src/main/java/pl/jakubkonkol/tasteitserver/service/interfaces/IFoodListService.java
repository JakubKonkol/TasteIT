package pl.jakubkonkol.tasteitserver.service.interfaces;

import pl.jakubkonkol.tasteitserver.dto.FoodListDto;
import pl.jakubkonkol.tasteitserver.dto.PostDto;

import java.util.List;

public interface IFoodListService {
    FoodListDto createFoodList(String sessionToken, String name);
    FoodListDto getFoodList(String sessionToken, String foodListId);
    List<FoodListDto> getAllFoodLists(String sessionToken);
    List<FoodListDto> getAllFoodListsSimpleInfo(String sessionToken);
    void addPostToFoodlist(String sessionToken, String foodListId, PostDto postId);
    void deletePostInFoodlist(String sessionToken, String foodListId, PostDto postId);
    void updateFoodlistName(String sessionToken, String foodListId, FoodListDto name);
    void deleteFoodList(String sessionToken, String foodListId);
}
