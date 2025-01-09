package pl.jakubkonkol.tasteitserver.badgeHandler;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.jakubkonkol.tasteitserver.model.enums.BadgeType;
import pl.jakubkonkol.tasteitserver.repository.PostRepository;
import pl.jakubkonkol.tasteitserver.service.BadgeService;
import pl.jakubkonkol.tasteitserver.service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;


@Aspect
@Component
public class BadgeTrackingAspect {
    @Autowired
    private BadgeService badgeService;
    @Autowired
    private UserService userService;
    @Autowired
    private PostRepository postRepository;

    private final Map<BadgeType, Consumer<String>> badgeLogicMap = new HashMap<>();

    public BadgeTrackingAspect() {
        badgeLogicMap.put(BadgeType.FIRST_RECIPE, this::firstRecipe);
//        badgeLogicMap.put("badge_002", this::risingChefFood);
//        badgeLogicMap.put("badge_003", this::risingChefFoodDrink);
        badgeLogicMap.put(BadgeType.MASTER_CHEF, this::masterChef);
//        badgeLogicMap.put("badge_005", this::popularRecipe);
//        badgeLogicMap.put("badge_006", this::communityFavourite);
//        badgeLogicMap.put("badge_007", this::topCommenter);
//        badgeLogicMap.put("badge_008", this::tasteInfluencer);
//        badgeLogicMap.put("badge_009", this::culinaryStar);
//        badgeLogicMap.put("badge_010", this::helpfulChef);
//        badgeLogicMap.put("badge_011", this::socialButterfly);
//        badgeLogicMap.put("badge_012", this::recipeConnoisseur);
    }



    @Before("@annotation(annotation) && args(.., sessionToken)")
    //TODO sprawdzic to miejsce moze trzeba zmienic tak aby akceptowal wiele adnotacji
    public void handleBadgeTracking(JoinPoint joinPoint, String sessionToken,TrackBadgesProgress annotation) {

        BadgeType[] badges = annotation.badges();
        for (BadgeType type : badges) {
            if (badgeLogicMap.containsKey(type)) {
                badgeLogicMap.get(type).accept(sessionToken); // Wywołanie logiki dla odznaki
            }
        }

        //TODO: gdy są 2 adnotacje debugger nie łapie nic w handleBadgeTracking
        //todo naprawić to, z 1 adnotacją dział wszystko super
    }

    private void firstRecipe(String sessionToken) {
        String userId = userService.getCurrentUserBySessionToken(sessionToken).getUserId();
        if (userService.getCurrentUserBySessionToken(sessionToken).getBadges().stream().noneMatch(badge -> badge.getBadgeId().equals(BadgeType.FIRST_RECIPE)))
            badgeService.grantBadgeToUser(BadgeType.FIRST_RECIPE, userId, sessionToken);
    }

    private void masterChef(String sessionToken) {
        String userId = userService.getCurrentUserBySessionToken(sessionToken).getUserId();
        if (userService.getCurrentUserBySessionToken(sessionToken).getBadges().stream().noneMatch(badge -> badge.getBadgeId().equals(BadgeType.MASTER_CHEF)))
            //TODO: fajnie byloby dawac id z zewnatrz tak by sie nie pomylić przy pisaniu oraz moze z zrobić z tego bardziej generyczną metodę pasującą dla wielu odznak
            badgeService.grantBadgeToUser(BadgeType.MASTER_CHEF, userId, sessionToken);
        else
            badgeService.updateBadgeProgress(sessionToken, BadgeType.MASTER_CHEF);

    }


}


/*
*
*
*
*
*
*
*
* */
