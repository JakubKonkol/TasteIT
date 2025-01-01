package pl.jakubkonkol.tasteitserver.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
        import pl.jakubkonkol.tasteitserver.dto.*;
        import pl.jakubkonkol.tasteitserver.model.enums.PostType;
import pl.jakubkonkol.tasteitserver.service.IngredientService;
import pl.jakubkonkol.tasteitserver.service.PostService;
import pl.jakubkonkol.tasteitserver.service.TagService;
import pl.jakubkonkol.tasteitserver.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/creator")
@RequiredArgsConstructor
public class CreatorController {
    private final PostService postService;
    private final UserService userService;
    private final TagService tagService;
    private final IngredientService ingredientService;

    @GetMapping("/any")
    public ResponseEntity<List<PostDto>> searchPostsWithAnyIngredient(@RequestParam List<String> ingredientNames,
                                                        @RequestHeader("Authorization") String sessionToken) {
        var foundPostsDto = postService.searchPostsWithAnyIngredient(ingredientNames, sessionToken);
        return ResponseEntity.ok(foundPostsDto);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PostDto>> searchPostsWithAllIngredients(@RequestParam List<String> ingredientNames,
                                                                              @RequestHeader("Authorization") String sessionToken) {
        var pageDto = postService.searchPostsWithAllIngredients(ingredientNames, sessionToken);
        return ResponseEntity.ok(pageDto);
    }
}
