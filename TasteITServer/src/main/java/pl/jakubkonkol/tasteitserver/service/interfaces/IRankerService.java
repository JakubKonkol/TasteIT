package pl.jakubkonkol.tasteitserver.service.interfaces;

import pl.jakubkonkol.tasteitserver.dto.PageDto;
import pl.jakubkonkol.tasteitserver.dto.PostDto;

public interface IRankerService {
    PageDto<PostDto> rankPosts(Integer page, Integer size, String sessionToken);
}
