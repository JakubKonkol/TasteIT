package pl.jakubkonkol.tasteitserver.apitools;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import pl.jakubkonkol.tasteitserver.factory.AdminUserFactory;
import pl.jakubkonkol.tasteitserver.repository.UserRepository;
import pl.jakubkonkol.tasteitserver.service.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class DBuilder {
    private final FoodFetcher foodFetcher;
    private final IngredientFetcher ingredientFetcher;
    private final DrinkFetcher drinkFetcher;
    private final IngredientService ingredientService;
    private final PostService postService;
    private final LikeService likeService;
    private final CommentService commentService;
    private final AdminUserFactory adminUserFactory;
    private final TagService tagService;

    private static final Logger LOGGER = Logger.getLogger(DBuilder.class.getName());

    @PostConstruct //this always should be active
    public void createDefaultAdminAccount() throws IOException{
        adminUserFactory.CreateAdmin();
    }
    @PostConstruct
    public void buildDataBase() throws IOException {
        ingredientService.deleteAll();
        commentService.deleteAll();
        likeService.deleteAll();
        postService.deleteAll();
        tagService.deleteAll();
        LOGGER.log(Level.INFO, "Database cleared, building new one");
        tagService.saveBasicTags();
        foodFetcher.populateDBWithFood();
        ingredientFetcher.populateDBWithIngredients();
        drinkFetcher.populateDBWithDrinks();

        LOGGER.log(Level.INFO, "Database built");
    }
}
