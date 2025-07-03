package pl.jakubkonkol.tasteitserver.model.value;

import com.google.common.base.Function;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import pl.jakubkonkol.tasteitserver.dto.BadgeDto;
import pl.jakubkonkol.tasteitserver.service.BadgeDataCollector;

import java.util.Objects;

@Document
@Getter
@Setter
@NoArgsConstructor
public class BadgeBlueprint {
    private static int nextId = 1;

    @Id
    private int id;
    private String badgeName;
    private String description;
    private String imageUrl;
    private Integer goalValue;
    private Function<BadgeDataCollector, Integer> valueCounter;

    public BadgeBlueprint(String badgeName, String description,  Integer goalValue,
                          Function<BadgeDataCollector, Integer> valueCounter) {
        id = nextId++;
        this.badgeName = badgeName;
        this.description = description;
        imageUrl = "";
        this.goalValue = goalValue;
        this.valueCounter = valueCounter;
    }

    public int countValue(BadgeDataCollector collector) {
        if (valueCounter == null) {
            return 0;
        }
        return valueCounter.apply(collector);
    }

    public BadgeDto.BadgeDtoBuilder toDto() {
        return BadgeDto.builder()
                .id(id)
                .name(badgeName)
                .description(description)
                .image(imageUrl)
                .goalValue(goalValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BadgeBlueprint that = (BadgeBlueprint) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void setValueCounter(Function<BadgeDataCollector, Integer> valueCounter) {
        this.valueCounter = valueCounter;
    }

}
