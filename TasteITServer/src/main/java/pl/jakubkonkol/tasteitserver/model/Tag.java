package pl.jakubkonkol.tasteitserver.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import pl.jakubkonkol.tasteitserver.model.enums.TagType;

@Document
@Data
public class Tag {
    @Id
    private String tagId;
    private String tagName;
    private TagType tagType;
}
