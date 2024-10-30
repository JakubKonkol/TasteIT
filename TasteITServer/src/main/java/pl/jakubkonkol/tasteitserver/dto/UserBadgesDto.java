package pl.jakubkonkol.tasteitserver.dto;

import lombok.Data;
import pl.jakubkonkol.tasteitserver.model.Badge;

import java.util.List;

@Data
public class UserBadgesDto {
    private List<Badge> badges;
}
