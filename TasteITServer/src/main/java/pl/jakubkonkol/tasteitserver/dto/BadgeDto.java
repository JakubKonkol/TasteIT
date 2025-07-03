package pl.jakubkonkol.tasteitserver.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class BadgeDto {
    private int id;
    private String name;
    private String description;
    private String image;
    private int value;
    private int goalValue;
    private boolean earned;
    private Date earnedDate;
}
