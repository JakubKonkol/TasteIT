package pl.jakubkonkol.tasteitserver.data;

import pl.jakubkonkol.tasteitserver.model.BadgeBlueprint;

import java.util.List;

public class BadgeData {
    public static List<BadgeBlueprint> badgeBlueprintData = List.of(
            //BluePrinty odznak, bez statusu
            new BadgeBlueprint("badge_001", "First Recipe", "Added your first recipe to the community!", "", 1),
            new BadgeBlueprint("badge_002", "Rising Chef (Food)", "Posted 10 food recipes. Keep cooking and sharing!", "", 10),
            new BadgeBlueprint("badge_003", "Rising Chef (Drinks)", "Posted 10 drink recipes. Keep mixing and sharing!", "", 10),
            new BadgeBlueprint("badge_004", "Master Chef", "Posted 50 recipes in total. A true culinary expert!", "", 50),
            new BadgeBlueprint("badge_005", "Popular Recipe", "One of your recipes reached 100 likes!", "", 100),
            new BadgeBlueprint("badge_006", "Community Favorite", "Achieved 500 likes across all recipes.", "", 500),
            new BadgeBlueprint("badge_007", "Top Commenter", "Left 50 comments. You're a key part of our community!", "", 50),
            new BadgeBlueprint("badge_008", "Taste Influencer", "Gained 10 followers. Your recipes are a hit!", "", 10),
            new BadgeBlueprint("badge_009", "Culinary Star", "Reached 100 followers. You inspire many chefs!", "", 100),
            new BadgeBlueprint("badge_010", "Helpful Chef", "Received 25 likes on your comments.", "", 25),
            new BadgeBlueprint("badge_011", "Social Butterfly", "Followed 10 users.", "", 10),
            new BadgeBlueprint("badge_012", "Recipe Connoisseur", "Saved 10 recipes to your collection.", "", 10)
    );
}
