package pl.jakubkonkol.tasteitserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Badge {
    @Id
    private String badgeId;
    private String badgeName;
    private String description;
    private String imageUrl;
}
