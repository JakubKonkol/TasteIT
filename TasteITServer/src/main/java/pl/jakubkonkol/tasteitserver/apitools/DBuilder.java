package pl.jakubkonkol.tasteitserver.apitools;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import pl.jakubkonkol.tasteitserver.factory.AdminUserFactory;
import pl.jakubkonkol.tasteitserver.service.interfaces.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class allows you to create sample data by downloading it from themealdb.com and thecocktaildb.com,
 * which is a temporary solution
 */
@Component
@RequiredArgsConstructor
public class DBuilder {
    private final FoodFetcher foodFetcher;
    private final IngredientFetcher ingredientFetcher;
    private final DrinkFetcher drinkFetcher;
    private final IIngredientService ingredientService;
    private final IPostService postService;
    private final ILikeService likeService;
    private final ICommentService commentService;
    private final AdminUserFactory adminUserFactory;
    private final ITagService tagService;

    private static final Logger LOGGER = Logger.getLogger(DBuilder.class.getName());

    @PostConstruct //this always should be active
    public void createDefaultAdminAccount() {
        try {
            adminUserFactory.CreateAdmin();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Uncomment @PostConstruct if u want to populate database with data from API
     */
    @PostConstruct
    public void buildDataBase() {
         try {
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
         } catch (IOException e){
             LOGGER.log(Level.SEVERE, e.getMessage());
         }
     }
}
