package pl.jakubkonkol.tasteitserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubkonkol.tasteitserver.dto.*;
import pl.jakubkonkol.tasteitserver.service.interfaces.IIngredientService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IPostService;
import pl.jakubkonkol.tasteitserver.service.interfaces.ITagService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IUserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {
    private final IPostService postService;
    private final IUserService userService;
    private final ITagService tagService;
    private final IIngredientService ingredientService;

    @GetMapping("/posts")
    public ResponseEntity<PageDto<PostDto>> searchPosts(@RequestParam String query,
                                                        @RequestParam(required = false) String type,
                                                        @RequestHeader("Authorization") String sessionToken,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        PageDto<PostDto> pageDto = postService.searchPosts(query, type, sessionToken, page, size);
        return ResponseEntity.ok(pageDto);
    }

    @GetMapping("/users")
    public ResponseEntity<PageDto<UserReturnDto>> searchUsers(@RequestParam String query,
                                                        @RequestHeader("Authorization") String sessionToken,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        PageDto<UserReturnDto> pageDto = userService.searchUsersByDisplayName(query, sessionToken, page, size);
        return ResponseEntity.ok(pageDto);
    }

    @GetMapping("/tags")
    public ResponseEntity<List<TagDto>> searchTags(@RequestParam String query) {
        List<TagDto> tagDtos = tagService.searchTagsByName(query);
        return ResponseEntity.ok(tagDtos);
    }

    @GetMapping("/tags/posts")
    public ResponseEntity<PageDto<PostDto>> searchPostsByTag(@RequestParam String tagId,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size) {
        PageDto<PostDto> pageDto = postService.searchPostsByTagName(tagId, page, size);
        return ResponseEntity.ok(pageDto);
    }

    @GetMapping("/ingredients")
    public ResponseEntity<PageDto<IngredientDto>> searchIngredientsByName(@RequestParam String query,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size) {
        PageDto<IngredientDto> pageDto = ingredientService.searchIngredientsByName(query, page, size);
        return ResponseEntity.ok(pageDto);
    }

    @GetMapping("/exclude_ingredients/posts")
    public ResponseEntity<PageDto<PostDto>> searchIngredientsByName(@RequestParam List<String> ingredientNames,
                                                                          @RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "20") int size) {
        PageDto<PostDto> pageDto = postService.getPostsExcludingIngredients(ingredientNames, page, size);
        return ResponseEntity.ok(pageDto);
    }
}
