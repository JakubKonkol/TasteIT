package pl.jakubkonkol.tasteitserver.data;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jakubkonkol.tasteitserver.model.enums.PostType;
import pl.jakubkonkol.tasteitserver.model.value.BadgeBlueprint;
import pl.jakubkonkol.tasteitserver.repository.BadgeBlueprintRepository;
import pl.jakubkonkol.tasteitserver.repository.BadgeRepository;

import java.util.ArrayList;
import java.util.List;
@Component
@RequiredArgsConstructor
public class BadgeData {
    private List<BadgeBlueprint> badgeBlueprintData = new ArrayList<>();
    private final BadgeBlueprintRepository badgeBlueprintRepository;


   @PostConstruct
    public void initBadges(){
       var badges = List.of(new BadgeBlueprint("First Recipe", "Added your first recipe to the community!",
                       1,collector -> collector.countPosts()),
               new BadgeBlueprint("Rising Chef (Food)", "Posted 10 food recipes. Keep cooking and sharing!",
                       10, collector -> collector.countPostsBy(PostType.FOOD)),
               new BadgeBlueprint("Rising Chef (Drinks)", "Posted 10 drink recipes. Keep mixing and sharing!",
                       10, collector -> collector.countPostsBy(PostType.DRINK)),
               new BadgeBlueprint("Master Chef", "Posted 50 recipes in total. A true culinary expert!",
                       50, collector -> collector.countPosts()),
               new BadgeBlueprint("Popular Recipe", "One of your recipes reached 100 likes!",
                        100, collector -> collector.countMaxLikes()),
               new BadgeBlueprint("Community Favorite", "Achieved 500 likes across all recipes.",
                        500, collector -> collector.countLikes()),
               new BadgeBlueprint("Top Commenter", "Left 50 comments. You're a key part of our community!",
                       50, collector -> collector.countComments()),
               new BadgeBlueprint("Taste Influencer", "Gained 10 followers. Your recipes are a hit!",
                        10, collector -> collector.countFollowers()),
               new BadgeBlueprint("Culinary Star", "Reached 100 followers. You inspire many chefs!",
                        100, collector -> collector.countFollowers()),
               new BadgeBlueprint("Social Butterfly", "Followed 10 users.",
                      10, collector -> collector.countFollowed()),
               new BadgeBlueprint("Recipe Connoisseur", "Saved 10 recipes to your collection.",
                       10,collector -> collector.countSaved()));
       badgeBlueprintData.addAll(badges);
       updateDatabase();
   }

    private void updateDatabase() {
        if (badgeBlueprintRepository.count() == 0) {
            badgeBlueprintRepository.saveAll(badgeBlueprintData);
        }
    }


    public List<BadgeBlueprint> getBadgeBlueprintData() {
        return badgeBlueprintData;
    }
}

/*
*   recipe tag:any  01,04
*   recipe tag:food 02,
*   recipe tag:drink 03
*   one recipe likes min 05
*
*
*
*
*
* */