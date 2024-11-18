package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.apitools.IngredientFetcher;
import pl.jakubkonkol.tasteitserver.data.BadgeData;
import pl.jakubkonkol.tasteitserver.dto.BadgeDto;
import pl.jakubkonkol.tasteitserver.dto.UserReturnDto;
import pl.jakubkonkol.tasteitserver.model.Badge;
import pl.jakubkonkol.tasteitserver.model.BadgeBlueprint;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.repository.BadgeRepository;
import pl.jakubkonkol.tasteitserver.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class BadgeService {
    private static final Logger LOGGER = Logger.getLogger(IngredientFetcher.class.getName());
    private final BadgeRepository badgeRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    public void saveBadgeData(){
        List<BadgeBlueprint> badgeBlueprintDataList = BadgeData.badgeBlueprintData;
        Pattern pattern = Pattern.compile("\\d+"); // Liczby w opisie
        Matcher matcher;

        for (int i = 0; i < badgeBlueprintDataList.size(); i++) {
            BadgeBlueprint badgeBlueprint = new BadgeBlueprint();
            badgeBlueprint.setBadgeId(badgeBlueprintDataList.get(i).getBadgeId());
            badgeBlueprint.setBadgeName(badgeBlueprintDataList.get(i).getBadgeName());
            badgeBlueprint.setDescription(badgeBlueprintDataList.get(i).getDescription());
            badgeBlueprint.setImageUrl(badgeBlueprintDataList.get(i).getImageUrl());
            if (badgeBlueprint.getBadgeId().equals("badge_001")) {
                badgeBlueprint.setGoalValue(1); // badge_001 jako jedyny nie ma liczby, ma słowo 'pierwszy'
            } else {
                matcher = pattern.matcher(badgeBlueprint.getDescription());
                if (matcher.find()) {
                    int goalValue = Integer.parseInt(matcher.group());
                    badgeBlueprint.setGoalValue(goalValue);
                } else {
                    badgeBlueprint.setGoalValue(0);
                }
            }
            badgeRepository.save(badgeBlueprint);
            System.out.println(badgeBlueprint);
        }
        LOGGER.log(Level.INFO, "Badges saved");
    }



    public void grantBadgeToUser(String badgeId, String userId, String sessionToken) {
        User user = userService.getCurrentUserBySessionToken(sessionToken);
        List<BadgeBlueprint> badgeBlueprints = badgeRepository.findAll();
        BadgeBlueprint badgeBlueprint = badgeBlueprints.stream().filter(b -> b.getBadgeId().equals(badgeId))
                .findFirst()
                .orElseThrow();

        Badge badge = Badge.builder()
                .badgeId(badgeBlueprint.getBadgeId())
                .badgeName(badgeBlueprint.getBadgeName())
                .description(badgeBlueprint.getDescription())
                .imageUrl(badgeBlueprint.getImageUrl())
                .goalValue(badgeBlueprint.getGoalValue())
                .currentValue(1) // TODO w przyszłości currentValue powinno byc przypisywane autmoatycznie jakaś @Adnotacja albo metoda
                .build();

        if (!user.getBadges().contains(badge)) {
            List<Badge> updatedBadges = new ArrayList<>(user.getBadges());
            updatedBadges.add(badge);

            userService.updateUserBadges(userId, updatedBadges);
        } else {
            LOGGER.log(Level.INFO, "Badge already granted or badge not found.");
        }
    }

    public void updateBadgeProgress(String sessionToken, String badgeId) {
        UserReturnDto userReturnDto = userService.getCurrentUserDtoBySessionToken(sessionToken);
        List<Badge> userBadges = userReturnDto.getBadges();
        Badge updatedBadge = userBadges.stream()
                .filter(b -> b.getBadgeId().equals(badgeId))
                .findFirst()
                .orElseThrow();
        if (updatedBadge.getCurrentValue() != updatedBadge.getGoalValue()) {
            updatedBadge.setCurrentValue(updatedBadge.getCurrentValue()+1);
            userBadges = List.of(updatedBadge);
            userService.updateUserBadges(userReturnDto.getUserId(), userBadges);
        }
    }


    private BadgeDto convertToDto(BadgeBlueprint badgeBlueprint) { //probably won't use
        return modelMapper.map(badgeBlueprint, BadgeDto.class);
    }

}
