package pl.jakubkonkol.tasteitserver.model;


import lombok.Data;
import org.springframework.data.mongodb.core.mapping.DBRef;
import pl.jakubkonkol.tasteitserver.dto.BadgeDto;
import pl.jakubkonkol.tasteitserver.model.value.BadgeBlueprint;

import java.util.Date;
import java.util.Objects;

@Data
public class Badge {
    private int id;
    @DBRef
    private BadgeBlueprint badgeBlueprint;
    private Date earningDate;

    public Badge() {

    }

    public Badge(BadgeBlueprint badgeBlueprint) {
        this.id = badgeBlueprint.getId();
        this.badgeBlueprint = badgeBlueprint;
        earningDate = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Badge badge = (Badge) o;
        return Objects.equals(id, badge.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public BadgeDto toDto() {
        return badgeBlueprint.toDto()
                .earned(true)
                .earnedDate(earningDate)
                .value(badgeBlueprint.getGoalValue())
                .build();
    }
}