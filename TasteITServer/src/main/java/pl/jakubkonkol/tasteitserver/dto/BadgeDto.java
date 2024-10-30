package pl.jakubkonkol.tasteitserver.dto;

import lombok.Data;

@Data
public class BadgeDto {
    private String badgeId;
    private String badgeName;
    private String description;
    private String imageUrl;
}
