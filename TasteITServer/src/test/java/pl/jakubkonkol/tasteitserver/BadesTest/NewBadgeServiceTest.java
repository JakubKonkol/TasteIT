package pl.jakubkonkol.tasteitserver.BadesTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.springframework.boot.test.context.SpringBootTest;
import pl.jakubkonkol.tasteitserver.data.BadgeData;
import pl.jakubkonkol.tasteitserver.dto.BadgeDto;
import pl.jakubkonkol.tasteitserver.model.Badge;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.model.value.BadgeBlueprint;
import pl.jakubkonkol.tasteitserver.service.NewBadgeService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class NewBadgeServiceTest {
    private NewBadgeService badgeService;
    private BadgeData badgeData;

    @BeforeEach
    void init() {
        badgeData = mock(BadgeData.class);
        badgeService = new NewBadgeService(badgeData);
    }

    @Test
    void shouldReturnAlreadyEarnedBadgeUnchanged() {
        User user = new User();

        BadgeBlueprint bp = mock(BadgeBlueprint.class);
        when(bp.getId()).thenReturn(1);
        when(bp.getGoalValue()).thenReturn(1);
        when(bp.countValue(any())).thenReturn(0);
        when(bp.toDto()).thenReturn(BadgeDto.builder().id(1).goalValue(1).earned(true));

        user.addEarnedBadge(new Badge(bp));
        when(badgeData.getBadgeBlueprintData()).thenReturn(List.of(bp));

        List<BadgeDto> result = badgeService.updateBadges(user);

        assertEquals(1, result.size());
        assertTrue(result.get(0).isEarned());
        assertTrue(user.getEarnedBadgeBy(1).isPresent());
    }

    @Test
    void shouldAddNewlyAchievedBadge() {
        User user = new User();

        BadgeBlueprint bp = mock(BadgeBlueprint.class);
        when(bp.getId()).thenReturn(2);
        when(bp.getGoalValue()).thenReturn(5);
        when(bp.countValue(any())).thenReturn(5);
        when(bp.toDto()).thenReturn(BadgeDto.builder().id(2).goalValue(5));

        when(badgeData.getBadgeBlueprintData()).thenReturn(List.of(bp));

        List<BadgeDto> result = badgeService.updateBadges(user);

        assertEquals(1, result.size());
        assertTrue(result.get(0).isEarned());
        assertTrue(user.getEarnedBadgeBy(2).isPresent());
    }

    @Test
    void shouldReturnBadgeInProgress() {
        User user = new User();

        BadgeBlueprint bp = mock(BadgeBlueprint.class);
        when(bp.getId()).thenReturn(3);
        when(bp.getGoalValue()).thenReturn(10);
        when(bp.countValue(any())).thenReturn(3);
        when(bp.toDto()).thenReturn(BadgeDto.builder().id(3).goalValue(10));

        when(badgeData.getBadgeBlueprintData()).thenReturn(List.of(bp));

        List<BadgeDto> result = badgeService.updateBadges(user);

        BadgeDto dto = result.get(0);
        assertFalse(dto.isEarned());
        assertEquals(3, dto.getValue());
        assertTrue(user.getEarnedBadgeBy(3).isEmpty());
    }
}
