package pl.jakubkonkol.tasteitserver.apitools;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.jakubkonkol.tasteitserver.exception.ApiRequestException;
import pl.jakubkonkol.tasteitserver.factory.IngredientDrinkFactory;
import pl.jakubkonkol.tasteitserver.model.Ingredient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import pl.jakubkonkol.tasteitserver.service.interfaces.IIngredientService;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * @Author: Jakub Konkol
 * @Author: Mikołaj Kawczyński
 * Fetches ingredients from thecocktaildb.com API and saves them to the database
 */
@Component
@RequiredArgsConstructor
public class IngredientFetcher {
    private static final Logger LOGGER = Logger.getLogger(IngredientFetcher.class.getName());
//    @Value("${ingredientFinder.url}")
    private final String ingredientFinderURL="https://www.thecocktaildb.com/api/json/v1/1/search.php?i=";
//    @Value("${ingredientList.url}")
    private final String ingredientListURL="https://www.thecocktaildb.com/api/json/v1/1/list.php?i=list";
    private final IIngredientService ingredientService;
    private final IngredientDrinkFactory ingredientFactory;
    private final OkHttpClient client;

    public void populateDBWithIngredients(){
        var ingredients = fetchIngredients();
        ingredientService.saveAllIngredients(ingredients);
        LOGGER.log(Level.INFO, "Alcohol ingredients saved");
    }

    private List<Ingredient> fetchIngredients(){
        var req = new Request.Builder().url(ingredientListURL).build();
        try (var res = client.newCall(req).execute()){
            if (!res.isSuccessful()) {
                throw new ApiRequestException("Request failed with HTTP error code: " + res.code());
            }
            return parseIngredients(res.body().string());
        }catch (IOException e){
            throw new ApiRequestException("Failed to fetch ingredient data: " + e.getMessage());
        }
    }
    private List<Ingredient> parseIngredients(String rawData){
        var ingredientData = new JSONObject(rawData);
        List<Ingredient> ingredientsList = new ArrayList<>();
        if (!ingredientData.has("drinks") || ingredientData.isNull("drinks")) {
            return ingredientsList;
        }
        var ingredients = ingredientData.getJSONArray("drinks");
        ingredients.forEach(ingredient -> {
            var ingredientObj = (JSONObject) ingredient;
            var ingredientName = ingredientObj.getString("strIngredient1");
            var newIngredient = fetchIngredientByName(ingredientName);
            ingredientsList.add(newIngredient);
        });

        return ingredientsList;
    }
    public Ingredient fetchIngredientByName(String name) {
        var url = (ingredientFinderURL + name).replace(" ", "%20");
        Request req = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(req).execute()) {
            if (response.code() == 429) {
                LOGGER.log(Level.WARNING, "Rate limit exceeded. Waiting before retrying...");
                Thread.sleep(1000);
                return fetchIngredientByName(name);
            }
            if (!response.isSuccessful()) {
                throw new RemoteException("Request failed with HTTP error code: " + response.code());
            }
            String rawData = response.body().string();
            return parseIngredient(rawData);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch ingredient data", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Ingredient parseIngredient(String rawData) {
        JSONObject ingredientData = new JSONObject(rawData);
        if (!ingredientData.has("ingredients") || ingredientData.isNull("ingredients")) {
            return null;
        }
        JSONObject ingredientObj = ingredientData.getJSONArray("ingredients").getJSONObject(0);
        return ingredientFactory.createIngredient(ingredientObj);
    }
}