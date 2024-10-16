package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.dto.FoodListDto;
import pl.jakubkonkol.tasteitserver.dto.PostDto;
import pl.jakubkonkol.tasteitserver.model.FoodList;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.repository.FoodListRepository;
import pl.jakubkonkol.tasteitserver.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodListService {
    private final ModelMapper modelMapper;
    private final FoodListRepository foodListRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public FoodListDto createFoodList(String sessionToken, String name) {
        var currentUser = userService.getCurrentUserBySessionToken(sessionToken);
        FoodList foodList = new FoodList();
        foodList.setUserId(currentUser.getUserId());
        foodList.setName(name);
        foodListRepository.save(foodList);
        return convertToDto(foodList);
    }
    public FoodListDto getFoodList(String foodListId) {
        FoodList foodList = foodListRepository.findByFoodListId(foodListId);
        return convertToDto(foodList);
    }
    public List<FoodListDto> getAllFoodListsFromUser(String sessionToken) {
        var currentUser = userService.getCurrentUserBySessionToken(sessionToken);
        List<FoodList> foodLists = foodListRepository.findAllByUserId(currentUser.getUserId());
        List<FoodListDto> foodListsDto = foodLists.stream()
                .map(this::convertToDto)
                .toList();
        return foodListsDto;
    }
    public void updatePostsInFoodlist(String foodListId, List<Post> posts) {
        var foodlist = foodListRepository.findByFoodListId(foodListId);
        foodlist.setPostsList(posts);
        foodListRepository.save(foodlist);
    }

    public void updateFoodlistName(String foodListId, String name) {
        var foodlist = foodListRepository.findByFoodListId(foodListId);
        foodlist.setName(name);
        foodListRepository.save(foodlist);
    }

    public void deleteFoodList(String foodListId) {
        var foodList = foodListRepository.findByFoodListId(foodListId);
        foodListRepository.delete(foodList);
    }

    private FoodListDto convertToDto(FoodList foodList) {
        FoodListDto foodListDto = modelMapper.map(foodList, FoodListDto.class);
        return foodListDto;
    }

    private FoodList convertToEntity(FoodListDto foodListDto) {
        return modelMapper.map(foodListDto, FoodList.class);
    }
}
