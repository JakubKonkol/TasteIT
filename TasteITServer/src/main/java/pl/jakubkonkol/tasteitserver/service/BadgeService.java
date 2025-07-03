package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.apitools.IngredientFetcher;
import pl.jakubkonkol.tasteitserver.data.BadgeData;
import pl.jakubkonkol.tasteitserver.dto.UserReturnDto;
import pl.jakubkonkol.tasteitserver.model.Badge;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.model.value.BadgeBlueprint;
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
    private final BadgeData badgeData;

    public void saveBadgeData() {
        List<BadgeBlueprint> badgeBlueprintDataList = badgeData.getBadgeBlueprintData();
        Pattern pattern = Pattern.compile("\\d+"); // Liczby w opisie
        Matcher matcher;

        for (int i = 0; i < badgeBlueprintDataList.size(); i++) {
            BadgeBlueprint badgeBlueprint = new BadgeBlueprint();
            badgeBlueprint.setId(badgeBlueprintDataList.get(i).getId());
            badgeBlueprint.setBadgeName(badgeBlueprintDataList.get(i).getBadgeName());
            badgeBlueprint.setDescription(badgeBlueprintDataList.get(i).getDescription());
            badgeBlueprint.setImageUrl(badgeBlueprintDataList.get(i).getImageUrl());
            if (badgeBlueprint.getId()==1) {
                badgeBlueprint.setGoalValue(1);
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
}
