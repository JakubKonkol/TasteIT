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
    public ResponseEntity<PageDto<PostDto>> searchPostsWithAnyIngredient(@RequestParam List<String> ingredientNames,
                                                        @RequestHeader("Authorization") String sessionToken,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        PageDto<PostDto> pageDto = postService.searchPostsWithAnyIngredient(ingredientNames, sessionToken, page, size);
        return ResponseEntity.ok(pageDto);
    }

    @GetMapping("/all")
    public ResponseEntity<PageDto<PostDto>> searchPostsWithAllIngredients(@RequestParam List<String> ingredientNames,
                                                                              @RequestHeader("Authorization") String sessionToken,
                                                                              @RequestParam(defaultValue = "0") int page,
                                                                              @RequestParam(defaultValue = "20") int size) {
        PageDto<PostDto> pageDto = postService.searchPostsWithAllIngredients(ingredientNames, sessionToken, page, size);
        return ResponseEntity.ok(pageDto);
    }
}
