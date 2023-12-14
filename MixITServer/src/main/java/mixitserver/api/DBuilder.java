package mixitserver.api;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import mixitserver.service.DrinkService;
import mixitserver.service.IngredientService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DBuilder {
    private final DrinkService drinkService;
    private final IngredientService ingredientService;

    @PostConstruct
    public void buildDataBase(){
        FetcherV2 f = new FetcherV2();
        f.fetchAll();
        ingredientService.saveAll(f.getIngredients());
        drinkService.saveAll(f.getDrinks());
        //ingredientService.saveAll(f.getIngredients());
    }
}
