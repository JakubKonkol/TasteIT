package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.annotation.RegisterAction;
import pl.jakubkonkol.tasteitserver.dto.FoodListDto;
import pl.jakubkonkol.tasteitserver.dto.PostDto;
import pl.jakubkonkol.tasteitserver.exception.ResourceNotFoundException;
import pl.jakubkonkol.tasteitserver.model.FoodList;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.repository.PostRepository;
import pl.jakubkonkol.tasteitserver.repository.UserRepository;
import pl.jakubkonkol.tasteitserver.service.interfaces.IFoodListService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IUserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodListService implements IFoodListService {
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final IUserService userService;
    private final PostRepository postRepository;

    public FoodListDto createFoodList(String sessionToken, FoodListDto foodListDto) {
        var currentUser = userService.getCurrentUserBySessionToken(sessionToken);

        FoodList foodList = new FoodList();
        foodList.setName(foodListDto.getName());
        currentUser.getFoodLists().add(foodList);
        userRepository.save(currentUser);
        return convertToDto(foodList);
    }

    public FoodListDto getFoodList(String sessionToken, String foodListId) {
        var currentUser = userService.getCurrentUserBySessionToken(sessionToken);

        FoodList foodList = currentUser.getFoodLists()
                .stream()
                .filter(f -> f.getFoodListId().equals(foodListId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("FoodList with id " + foodListId + " not found"));

        return convertToDto(foodList);
    }
    public List<FoodListDto> getAllFoodLists(String sessionToken) {
        var currentUser = userService.getCurrentUserBySessionToken(sessionToken);

        var foodLists = currentUser.getFoodLists();
        return foodLists.stream()
                .map(this::convertToDto)
                .toList();
    }

    public List<FoodListDto> getAllFoodListsSimpleInfo(String sessionToken) {
        var currentUser = userService.getCurrentUserBySessionToken(sessionToken);

        var foodLists = currentUser.getFoodLists();
        return foodLists.stream()
                .map(f->{
                    var foodlistDto = new FoodListDto();
                    foodlistDto.foodListId = f.getFoodListId();
                    foodlistDto.name = f.getName();
                    foodlistDto.createdDate = f.getCreatedDate();
                    foodlistDto.postsCount = f.getPostsList().size();
                    return foodlistDto;
                })
                .toList();
    }

    @RegisterAction(actionType = "ADD_TO_FOODLIST")
    public void addPostToFoodlist(String sessionToken, String foodListId, PostDto postId) {
        var currentUser = userService.getCurrentUserBySessionToken(sessionToken);
        Post post = postRepository.findById(postId.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post with id " + postId + " not found"));

        currentUser.getFoodLists().stream()
                .filter(f -> f.getFoodListId().equals(foodListId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("FoodList with id " + foodListId + " not found"))
                .getPostsList()
                .add(post);

        userRepository.save(currentUser);
    }

    public void deletePostInFoodlist(String sessionToken, String foodListId, PostDto postId) {
        var currentUser = userService.getCurrentUserBySessionToken(sessionToken);
        var post = postRepository.findById(postId.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post with id " + postId + " not found"));

        currentUser.getFoodLists().stream()
                .filter(f -> f.getFoodListId().equals(foodListId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("FoodList with id " + foodListId + " not found"))
                .getPostsList()
                .remove(post);

        userRepository.save(currentUser);
    }

    public void updateFoodlistName(String sessionToken, String foodListId, FoodListDto name) {
        var currentUser = userService.getCurrentUserBySessionToken(sessionToken);

        currentUser.getFoodLists().stream()
                .filter(f -> f.getFoodListId().equals(foodListId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("FoodList with id " + foodListId + " not found"))
                .setName(name.name);

        userRepository.save(currentUser);
    }

    public void deleteFoodList(String sessionToken, String foodListId) {
        var currentUser = userService.getCurrentUserBySessionToken(sessionToken);

        var foodListToDelete = currentUser.getFoodLists().stream()
                .filter(f -> f.getFoodListId().equals(foodListId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("FoodList with id " + foodListId + " not found"));


        currentUser.getFoodLists().remove(foodListToDelete);

        userRepository.save(currentUser);
    }

    private FoodListDto convertToDto(FoodList foodList) {
        return modelMapper.map(foodList, FoodListDto.class);
    }

    private FoodList convertToEntity(FoodListDto foodListDto) {
        return modelMapper.map(foodListDto, FoodList.class);
    }
}
