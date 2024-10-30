package pl.jakubkonkol.tasteitserver.data;

import pl.jakubkonkol.tasteitserver.model.Badge;

import java.util.List;

public class BadgeData {
    public static List<Badge> badgeData = List.of(
            new Badge("badge_001", "First Recipe", "Added your first recipe to the community!", ""),
            new Badge("badge_002", "Rising Chef (Food)", "Posted 10 food recipes. Keep cooking and sharing!", ""),
            new Badge("badge_003", "Rising Chef (Drinks)", "Posted 10 drink recipes. Keep mixing and sharing!", ""),
            new Badge("badge_004", "Master Chef", "Posted 50 recipes in total. A true culinary expert!", ""),
            new Badge("badge_005", "Popular Recipe", "One of your recipes reached 100 likes!", ""),
            new Badge("badge_006", "Community Favorite", "Achieved 500 likes across all recipes.", ""),
            new Badge("badge_007", "Top Commenter", "Left 50 comments. You're a key part of our community!", ""),
            new Badge("badge_008", "Taste Influencer", "Gained 10 followers. Your recipes are a hit!", ""),
            new Badge("badge_009", "Culinary Star", "Reached 100 followers. You inspire many chefs!", ""),
            new Badge("badge_010", "Helpful Chef", "Received 25 likes on your comments.", ""),
            new Badge("badge_011", "Social Butterfly", "Followed 10 users.", ""),
            new Badge("badge_012", "Recipe Connoisseur", "Saved 10 recipes to your collection.", "")
    );
}
