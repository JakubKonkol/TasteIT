package pl.jakubkonkol.tasteitserver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import pl.jakubkonkol.tasteitserver.model.enums.BadgeType;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Badge {
    @Id
    private String badgeId;
    private BadgeType badgeType;
    private String badgeName;
    private String description;
    private String imageUrl;
    private int goalValue;
    private int currentValue;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Badge badge = (Badge) o;
        return Objects.equals(badgeId, badge.badgeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(badgeId);
    }

    public String getBadgeId() {
        return badgeType.getId() + "";
    }
}
