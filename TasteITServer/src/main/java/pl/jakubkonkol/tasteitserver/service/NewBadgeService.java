package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.data.BadgeData;
import pl.jakubkonkol.tasteitserver.dto.BadgeDto;
import pl.jakubkonkol.tasteitserver.model.Badge;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.model.value.BadgeBlueprint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NewBadgeService {

    private final BadgeData badgeData;
    public List<BadgeDto> updateBadges(User user) {
        BadgeDataCollector badgeDataCollector = new BadgeDataCollector(user);
        List<BadgeBlueprint> badgeBlueprints = badgeData.getBadgeBlueprintData();
        List<BadgeDto> allBadges = new ArrayList<>();

        for (BadgeBlueprint badgeBlueprint : badgeBlueprints) {
            Optional<Badge> earnedBadge = user.getEarnedBadgeBy(badgeBlueprint.getId());
            if (earnedBadge.isPresent()) {
                allBadges.add(earnedBadge.get().toDto());
                continue;
            }
            int value = badgeBlueprint.countValue(badgeDataCollector);
            if (value>= badgeBlueprint.getGoalValue()) {
                Badge badge = new Badge(badgeBlueprint);
                user.addEarnedBadge(badge);
                allBadges.add(badge.toDto());
                continue;
            }
            allBadges.add(badgeBlueprint.toDto().value(value).build());
        }
        return allBadges;
    }
}
