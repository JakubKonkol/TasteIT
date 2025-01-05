package pl.jakubkonkol.tasteitserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubkonkol.tasteitserver.dto.FoodListDto;
import pl.jakubkonkol.tasteitserver.dto.PostDto;
import pl.jakubkonkol.tasteitserver.model.GenericResponse;
import pl.jakubkonkol.tasteitserver.service.interfaces.IFoodListService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/foodlist")
@RequiredArgsConstructor
public class FoodListController {
    private final IFoodListService foodListService;

    @PostMapping("/{name}")
    public ResponseEntity<FoodListDto> createFoodList(
            @RequestHeader("Authorization") final String sessionToken, @PathVariable String name) {
        FoodListDto foodListDto = foodListService.createFoodList(sessionToken, name);
        return ResponseEntity.ok(foodListDto);
    }

    @GetMapping("/{foodListId}")
    public ResponseEntity<FoodListDto> getFoodListById(
            @RequestHeader("Authorization") final String sessionToken,
            @PathVariable String foodListId) {
        FoodListDto foodListDto = foodListService.getFoodList(sessionToken, foodListId);
        return ResponseEntity.ok(foodListDto);
    }

    @GetMapping()
    public ResponseEntity<List<FoodListDto>> getAllFoodLists(
            @RequestHeader("Authorization") final String sessionToken) {
        List<FoodListDto> foodLists = foodListService.getAllFoodLists(sessionToken);
        return ResponseEntity.ok(foodLists);
    }

    @GetMapping("/simple")
    public ResponseEntity<List<FoodListDto>> getAllFoodListsSimpleInfo(
            @RequestHeader("Authorization") final String sessionToken) {
        List<FoodListDto> foodLists = foodListService.getAllFoodListsSimpleInfo(sessionToken);
        return ResponseEntity.ok(foodLists);
    }

    @PutMapping("/name/{foodListId}")
    public ResponseEntity<GenericResponse> updateFoodlistName(
            @RequestHeader("Authorization") final String sessionToken,
            @PathVariable String foodListId, @RequestBody FoodListDto name) {
        foodListService.updateFoodlistName(sessionToken, foodListId, name);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value()).
                message("Foodlist Updated")
                .build());
    }

    @DeleteMapping("/{foodListId}")
    public ResponseEntity<GenericResponse> deleteFoodList(
            @RequestHeader("Authorization") final String sessionToken,
            @PathVariable String foodListId) {
        foodListService.deleteFoodList(sessionToken, foodListId);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value()).
                message("Foodlist Deleted")
                .build());
    }

    @PostMapping("/post/{foodListId}")
    public ResponseEntity<GenericResponse> addPostToFoodlist(
            @RequestHeader("Authorization") final String sessionToken,
            @PathVariable String foodListId, @RequestBody PostDto postId) {
        foodListService.addPostToFoodlist(sessionToken, foodListId, postId);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value()).
                message("Foodlist Updated")
                .build());
    }

    @DeleteMapping("/post/{foodListId}")
    public ResponseEntity<GenericResponse> deletePostInFoodlist(
            @RequestHeader("Authorization") final String sessionToken,
            @PathVariable String foodListId, @RequestBody PostDto postId) {
        foodListService.deletePostInFoodlist(sessionToken, foodListId, postId);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value()).
                message("Foodlist Updated")
                .build());
    }
}
