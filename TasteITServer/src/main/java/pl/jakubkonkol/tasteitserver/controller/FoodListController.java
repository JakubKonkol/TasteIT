package pl.jakubkonkol.tasteitserver.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubkonkol.tasteitserver.dto.CommentDto;
import pl.jakubkonkol.tasteitserver.dto.FoodListDto;
import pl.jakubkonkol.tasteitserver.dto.PageDto;
import pl.jakubkonkol.tasteitserver.dto.PostDto;
import pl.jakubkonkol.tasteitserver.model.GenericResponse;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.Recipe;
import pl.jakubkonkol.tasteitserver.service.CommentService;
import pl.jakubkonkol.tasteitserver.service.FoodListService;
import pl.jakubkonkol.tasteitserver.service.LikeService;
import pl.jakubkonkol.tasteitserver.service.PostService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/foodlist")
@RequiredArgsConstructor
public class FoodListController {
    private final FoodListService foodListService;

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
        List<FoodListDto> foodLists = foodListService.getAllFoodLists(sessionToken); //TODO
        return ResponseEntity.ok(foodLists);
    }

    @PutMapping("/name/{foodListId}")
    public ResponseEntity<?> updateFoodlistName(
            @RequestHeader("Authorization") final String sessionToken,
            @PathVariable String foodListId, @RequestBody @Valid String name) {
        foodListService.updateFoodlistName(sessionToken, foodListId, name);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value()).
                message("Foodlist Updated")
                .build());
    }

    @DeleteMapping("/{foodListId}")
    public ResponseEntity<?> deleteFoodList(
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
    public ResponseEntity<?> addPostToFoodlist(
            @RequestHeader("Authorization") final String sessionToken,
            @PathVariable String foodListId, @RequestBody @Valid Post post) {
        foodListService.addPostToFoodlist(sessionToken, foodListId, post);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value()).
                message("Foodlist Updated")
                .build());
    }

    @DeleteMapping("/post/{foodListId}")
    public ResponseEntity<?> deletePostInFoodlist(
            @RequestHeader("Authorization") final String sessionToken,
            @PathVariable String foodListId, @RequestBody @Valid Post post) {
        foodListService.deletePostInFoodlist(sessionToken, foodListId, post);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value()).
                message("Foodlist Updated")
                .build());
    }
}
