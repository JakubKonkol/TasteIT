package pl.jakubkonkol.tasteitserver.service;

import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.model.enums.PostType;

public class BadgeDataCollector {

    private User user;

    public BadgeDataCollector(User user) {
        this.user = user;
    }

    public int countMaxLikes(){
        return user.getPostWithMaxLikes()
                .map(post -> post.getLikes().size())
                .orElse(0);
    }

    public int countPostsBy(PostType type){
        return user.getPostsBy(type)
                .size();
    }

    public int countPosts() {
        return user.getPosts().size();
    }

    public int countLikes(){
        return user.countAllLikesOnPosts();
    }

    public int countComments() {
        return user.getCreatedComments().size();
    }
    public int countFollowers() {
        return user.getFollowers().size();
    }

    public int countFollowed() {
        return user.getFollowing().size();
    }

    public int countSaved() {
        return user.countPostsInFoodlists();
    }

//                new BadgeBlueprint("badge_001", "First Recipe", "Added your first recipe to the community!", "", 1),
//            new BadgeBlueprint("badge_002", "Rising Chef (Food)", "Posted 10 food recipes. Keep cooking and sharing!", "", 10),
//            new BadgeBlueprint("badge_003", "Rising Chef (Drinks)", "Posted 10 drink recipes. Keep mixing and sharing!", "", 10),
//            new BadgeBlueprint("badge_004", "Master Chef", "Posted 50 recipes in total. A true culinary expert!", "", 50),
//            new BadgeBlueprint("badge_005", "Popular Recipe", "One of your recipes reached 100 likes!", "", 100),
//            new BadgeBlueprint("badge_006", "Community Favorite", "Achieved 500 likes across all recipes.", "", 500),
//            new BadgeBlueprint("badge_007", "Top Commenter", "Left 50 comments. You're a key part of our community!", "", 50),
//            new BadgeBlueprint("badge_008", "Taste Influencer", "Gained 10 followers. Your recipes are a hit!", "", 10),
//            new BadgeBlueprint("badge_009", "Culinary Star", "Reached 100 followers. You inspire many chefs!", "", 100),
//         nie ma like na kometarzach - usuniety
//            new BadgeBlueprint("badge_011", "Social Butterfly", "Followed 10 users.", "", 10),
//            new BadgeBlueprint("badge_012", "Recipe Connoisseur", "Saved 10 recipes to your collection.", "", 10)
}
