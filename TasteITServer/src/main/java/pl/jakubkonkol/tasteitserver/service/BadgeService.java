package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.apitools.IngredientFetcher;
import pl.jakubkonkol.tasteitserver.data.BadgeData;
import pl.jakubkonkol.tasteitserver.dto.BadgeDto;
import pl.jakubkonkol.tasteitserver.dto.UserBadgesDto;
import pl.jakubkonkol.tasteitserver.dto.UserReturnDto;
import pl.jakubkonkol.tasteitserver.model.Badge;
import pl.jakubkonkol.tasteitserver.repository.BadgeRepository;
import pl.jakubkonkol.tasteitserver.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class BadgeService {
    private static final Logger LOGGER = Logger.getLogger(IngredientFetcher.class.getName());
    private final BadgeRepository badgeRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    public void saveBadgeData(){
        List<Badge> badgeDataList = BadgeData.badgeData;
        for(int i = 0; i < BadgeData.badgeData.size(); i++){
            Badge badge = new Badge();
            badge.setBadgeId(badgeDataList.get(i).getBadgeId());
            badge.setBadgeName(badgeDataList.get(i).getBadgeName());
            badge.setDescription(badgeDataList.get(i).getDescription());
            badge.setImageUrl(badgeDataList.get(i).getImageUrl());
            badgeRepository.save(badge);
        }
        LOGGER.log(Level.INFO, "Badges saved");
    }

    public void grantBadgeToUser(String badgeId, String userId, String sessionToken) {
        UserReturnDto userReturnDto = userService.getUserDtoById(userId, sessionToken);
        Badge badge = BadgeData.badgeData.stream().filter(b -> b.getBadgeId().equals(badgeId)).findFirst().orElse(null);
        if (badge != null && !userReturnDto.getBadges().contains(badge)) {
            // Aktualizacja listy badge'y użytkownika
            List<Badge> updatedBadges = new ArrayList<>(userReturnDto.getBadges());
            updatedBadges.add(badge);

            // Utwórz DTO z aktualizowaną listą badge'y i zapisz do MongoDB
            UserBadgesDto userBadgesDto = new UserBadgesDto();
            userBadgesDto.setBadges(updatedBadges);
            userService.updateUserBadges(userId, userBadgesDto);

            // Pobierz zaktualizowanego użytkownika, aby potwierdzić zapisanie badge'a
            userReturnDto = userService.getUserDtoById(userId, sessionToken);
            if (userReturnDto.getBadges().contains(badge)) {
                LOGGER.log(Level.INFO, "Badge granted successfully.");
            } else {
                LOGGER.log(Level.WARNING, "Badge not found in user badges after update.");
            }
        } else {
            LOGGER.log(Level.INFO, "Badge already granted or badge not found.");
        }

//        UserReturnDto userReturnDto = userService.getUserDtoById(userId, sessionToken);
//        Badge badge = BadgeData.badgeData.stream().filter(b -> b.getBadgeId().equals(badgeId)).findFirst().orElse(null);
//
//        if (!userReturnDto.getBadges().contains(badge) && badge != null){
//            UserBadgesDto userBadgesDto = new UserBadgesDto();
//            List<Badge> badges = List.of(badge);
//            userReturnDto.setBadges(badges);
//            userService.updateUserBadges(userId, userBadgesDto);
//
//        }
//        else
//            LOGGER.log(Level.INFO, "Badge already granted");

    }


    private BadgeDto convertToDto(Badge badge) {
        return modelMapper.map(badge, BadgeDto.class);
    }

}
