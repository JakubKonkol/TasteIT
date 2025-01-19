package pl.jakubkonkol.tasteitserver.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubkonkol.tasteitserver.dto.*;
import pl.jakubkonkol.tasteitserver.service.interfaces.IPostService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/creator")
@RequiredArgsConstructor
public class CreatorController {
    private final IPostService postService;

    @PutMapping("/any")
    public ResponseEntity<List<PostDto>> searchPostsWithAnyIngredient(@RequestBody List<String> ingredientNames,
                                                        @RequestHeader("Authorization") String sessionToken) {
        var foundPostsDto = postService.searchPostsWithAnyIngredient(ingredientNames, sessionToken);
        return ResponseEntity.ok(foundPostsDto);
    }

    @PutMapping("/all")
    public ResponseEntity<List<PostDto>> searchPostsWithAllIngredients(@RequestBody List<String> ingredientNames,
                                                                              @RequestHeader("Authorization") String sessionToken) {
        var pageDto = postService.searchPostsWithAllIngredients(ingredientNames, sessionToken);
        return ResponseEntity.ok(pageDto);
    }
}
