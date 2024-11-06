package pl.jakubkonkol.tasteitserver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
//@Document
public class Badge {
    @Id
    private String badgeId;
    private String badgeName;
    private String description;
    private String imageUrl;
    private int goalValue;
    private int currentValue;
}
