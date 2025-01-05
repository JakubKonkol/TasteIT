package pl.jakubkonkol.tasteitserver.service.interfaces;

import pl.jakubkonkol.tasteitserver.dto.TagDto;
import pl.jakubkonkol.tasteitserver.model.Tag;

import java.util.List;
import java.util.Optional;

public interface ITagService {
    Tag save(Tag tag);
    List<Tag> getAll();
    Optional<Tag> findByName(String tagName);
    List<TagDto> searchTagsByName(String tagName);
    List<Tag> getBasicTags();
    void saveBasicTags();
    void deleteAll();
    Tag convertToEntity(TagDto tagDto);
}
