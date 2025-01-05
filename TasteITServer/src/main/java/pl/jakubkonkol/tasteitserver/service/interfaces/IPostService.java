package pl.jakubkonkol.tasteitserver.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import pl.jakubkonkol.tasteitserver.dto.PageDto;
import pl.jakubkonkol.tasteitserver.dto.PostDto;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.Recipe;
import pl.jakubkonkol.tasteitserver.model.projection.PostPhotoView;

import java.util.List;

public interface IPostService {
    void save(PostDto postDto);
    List<Post> saveAll(List<Post> posts);
    void deleteAll();
    List<Post> getAll();
    PostDto getPost(String postId, String sessionToken);
    PageDto<PostDto> getRandomPosts(Integer page, Integer size, String sessionToken);
    PageDto<PostDto> searchPosts(String title, String postType, String sessionToken, int page, int size);
    PageDto<PostDto> searchPostsByTagName(String tagId, Integer page, Integer size);
    PageDto<PostDto> getUserPosts(String userId, Integer page, Integer size);
    PageDto<PostDto> getPostDtoPageDto(List<Post> posts, Long total, Pageable pageable, String sessionToken);
    public PageDto<PostDto> getPostDtoPageDtoFromPostPhotoView(
            Page<PostPhotoView> postsPhotoViewPage, Pageable pageable);
    PageDto<PostDto> getPageDto(PageImpl<PostDto> pageImpl);
    Recipe getPostRecipe(String postId);
    List<PostDto> getPostsLikedByUser(String userId, String sessionToken);
    PostDto createPost(PostDto postDto, String sessionToken);
    PageDto<PostDto> getPostsExcludingIngredients(List<String> ingredientNames, Integer page, Integer size);
    PostDto convertToDto(Post post, String sessionToken);
    PageDto<PostDto> convertPostsToPageDto(String sessionToken, List<Post> posts, Pageable pageable);
}
