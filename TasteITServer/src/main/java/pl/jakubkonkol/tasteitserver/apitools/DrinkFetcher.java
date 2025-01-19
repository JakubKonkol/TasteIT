package pl.jakubkonkol.tasteitserver.apitools;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.jakubkonkol.tasteitserver.factory.PostDrinkFactory;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.service.interfaces.IPostService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IUserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Author: Jakub Konkol
 * @Author: Mikołaj Kawczyński
 * Fetches drinks from thecocktaildb.com API and saves them to the database
 */
@Component
@RequiredArgsConstructor
public class DrinkFetcher {
    private final OkHttpClient client;
    private final IPostService postService;
    private static final Logger LOGGER = Logger.getLogger(DrinkFetcher.class.getName());
//    @Value("${drinkfinder.url}")
    private final String drinkFinderURL = "https://thecocktaildb.com/api/json/v1/1/search.php?f=";
    private final PostDrinkFactory postFactory;
    private final IUserService userService;

    public void populateDBWithDrinks() throws IOException {
        var drinks = fetchDrinks();
        List<Post> posts = postService.saveAll(drinks);

        User admin = userService.getUserById("0");
        admin.getPosts().addAll(posts);
        userService.saveUser(admin);

        LOGGER.log(Level.INFO, "Drinks saved");
    }

    private List<Post> fetchDrinks() throws IOException {
        List<Post> drinkList = new ArrayList<>();
        for (char c = 'a'; c <= 'z'; c++) {
            var url = drinkFinderURL + c;
            drinkList.addAll(fetchDrinksByFirstLetter(url)) ;
        }

        return drinkList;
    }

    private List<Post> fetchDrinksByFirstLetter(String url) throws IOException {
        var req = new Request.Builder().url(url).build();
        try (var res = client.newCall(req).execute()) {
            if (res.code() == 429){
                LOGGER.warning("Too many requests, waiting 10 seconds");
                Thread.sleep(10000);
                return fetchDrinksByFirstLetter(url);
            }
            if (!res.isSuccessful()) {
                LOGGER.warning("Request failed with HTTP error code: " + res.code());
                return new ArrayList<>();
            }
            var rawData = res.body().string();
            if(StringUtils.isBlank(rawData)){
                return new ArrayList<>();
            }
            return parseDrink(rawData);
        } catch (IOException e) {
            LOGGER.warning("Failed to fetch drink data: " + e.getMessage());
            throw e;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Post> parseDrink(String rawData) {
        var drinkData = new JSONObject(rawData);
        List<Post> drinkList = new ArrayList<>();
        if (!drinkData.has("drinks") || drinkData.isNull("drinks")) {
            return drinkList;
        }
        var drinks = drinkData.getJSONArray("drinks");
        drinks.forEach(drinkItem ->{
            var drinkObj = (JSONObject) drinkItem;
            var newPost = postFactory.createPost(drinkObj);
            drinkList.add(newPost);
        });
        return drinkList;
    }
}