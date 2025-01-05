package pl.jakubkonkol.tasteitserver.service.interfaces;

import pl.jakubkonkol.tasteitserver.dto.PageDto;
import pl.jakubkonkol.tasteitserver.dto.PostDto;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.repository.UserActionRepository;

import java.util.Date;
import java.util.List;

public interface IRankerService {
    PageDto<PostDto> rankPosts(Integer page, Integer size, String sessionToken);
}
