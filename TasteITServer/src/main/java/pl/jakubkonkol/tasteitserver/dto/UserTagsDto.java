package pl.jakubkonkol.tasteitserver.dto;

import lombok.Data;
import pl.jakubkonkol.tasteitserver.model.Tag;

import java.util.List;

@Data
public class UserTagsDto {
    private List<Tag> tags;
}
