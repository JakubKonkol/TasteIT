package pl.jakubkonkol.tasteitserver.service;

import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.transaction.annotation.Transactional;
import pl.jakubkonkol.tasteitserver.dto.PageDto;
import pl.jakubkonkol.tasteitserver.dto.PostAuthorDto;
import pl.jakubkonkol.tasteitserver.dto.PostDto;
import pl.jakubkonkol.tasteitserver.exception.ResourceNotFoundException;
import pl.jakubkonkol.tasteitserver.model.FoodList;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.Recipe;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.model.projection.PostPhotoView;
import pl.jakubkonkol.tasteitserver.model.projection.UserShort;
import pl.jakubkonkol.tasteitserver.model.value.PostWithMatchCount;
import pl.jakubkonkol.tasteitserver.repository.LikeRepository;
import pl.jakubkonkol.tasteitserver.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.service.interfaces.IPostService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IUserService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService implements IPostService {
    private final MongoTemplate mongoTemplate;
    private final ModelMapper modelMapper;
    private final PostRepository postRepository;
    private final IUserService userService;
    private final LikeRepository likeRepository;

    @CacheEvict(value = {"posts", "postById", "userPosts", "postsByTag", "likedPosts", "postsAll"}, allEntries = true)
    public void save(PostDto postDto) {
        Post post = convertToEntity(postDto);
        postRepository.save(Objects.requireNonNull(post, "Post cannot be null"));
    }

    @CacheEvict(value = {"posts", "postById", "userPosts", "postsByTag", "likedPosts", "postsAll"}, allEntries = true)
    public List<Post> saveAll(List<Post> posts) {
        return postRepository.saveAll(
                Objects.requireNonNull(posts, "List of posts cannot be null"));
    }

    @CacheEvict(value = {"posts", "postById", "userPosts", "postsByTag", "likedPosts", "postsAll"}, allEntries = true)
    public void deleteAll() {
        postRepository.deleteAll();
    }

    @CacheEvict(value = {"posts", "postById", "userPosts", "postsByTag", "likedPosts", "postsAll"}, allEntries = true)
    @Transactional
    public void deleteOnePostById(String postId, String sessionToken) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post with id " + postId + " not found"));

        UserShort currentUser = userService.getCurrentUserShortBySessionToken(sessionToken);
        if (!post.getUserId().equals(currentUser.getUserId())) {
            throw new IllegalStateException(
                    "Post of id: " + postId + " does not belong to the user of id: " +
                            currentUser.getUserId());
        }

        Query userQuery = new Query();
        Update userUpdate = new Update().pull("posts", postId);
        mongoTemplate.updateMulti(userQuery, userUpdate, User.class);

        Query foodListQuery = new Query();
        Update foodListUpdate = new Update().pull("postsList", postId);
        mongoTemplate.updateMulti(foodListQuery, foodListUpdate, FoodList.class);

        postRepository.deleteById(postId);
    }

    @Cacheable(value = "postsAll", key = "'AllPosts'")
    public List<Post> getAll() {
        return postRepository.findAll();
    }

    @Cacheable(value = "postById", key = "#postId")
    public PostDto getPost(String postId, String sessionToken) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post with id " + postId + " not found"));

        if (post.getUserId() == null) throw new IllegalArgumentException("Post User ID cannot be null");

        return convertToDto(post, sessionToken);
    }

    public PageDto<PostDto> getRandomPosts(Integer page, Integer size, String sessionToken) {
        Pageable pageable = PageRequest.of(page, size);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.sample(size)
        );

        AggregationResults<Post> results = mongoTemplate.aggregate(aggregation, "post", Post.class);
        List<Post> posts = results.getMappedResults();

        if (posts.isEmpty()) {
            throw new ResourceNotFoundException("No random posts found in repository");
        }

        return convertPostsToPageDto(sessionToken, posts, pageable);
    }

    public PageDto<PostDto> searchPosts(String title, String postType, String sessionToken,
                                        int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Query query = new Query();
        query.addCriteria(Criteria.where("postMedia.title").regex(title, "i"));

        if (postType != null) {
            query.addCriteria(Criteria.where("postType").is(postType));
        }

        long total = mongoTemplate.count(query, Post.class);
        query.with(pageable);
        List<Post> posts = mongoTemplate.find(query, Post.class);

        return getPostDtoPageDto(posts, total, pageable, sessionToken);
    }

    public List<PostDto> searchPostsWithAnyIngredient(List<String> ingredientNames,
                                                      String sessionToken) {
        var foundPosts = postRepository.findAll();

        Set<String> ingredientNamesLower = ingredientNames.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        var filteredPosts = foundPosts.stream()
                .filter(post -> post.getRecipe() != null &&
                        post.getRecipe().getIngredientsWithMeasurements() != null)
                .filter(post -> post.getRecipe().getIngredientsWithMeasurements().stream()
                        .anyMatch(ingredient -> ingredientNamesLower.contains(
                                ingredient.getName().toLowerCase())))
                .map(post -> new PostWithMatchCount(post,
                        (int) post.getRecipe().getIngredientsWithMeasurements().stream()
                                .filter(ingredient -> ingredientNamesLower.contains(
                                        ingredient.getName().toLowerCase()))
                                .count()))
                .sorted(Comparator.comparingInt(PostWithMatchCount::matchCount).reversed())
                .map(PostWithMatchCount::post)
                .toList();

        return filteredPosts.stream()
                .map(post -> convertToDto(post, sessionToken))
                .toList();
    }

    public List<PostDto> searchPostsWithAllIngredients(List<String> ingredientNames,
                                                       String sessionToken) {
        var foundPosts = postRepository.findAll();

        Set<String> ingredientNamesLower = ingredientNames.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        var filteredPosts = foundPosts.stream()
                .filter(post -> post.getRecipe() != null &&
                        post.getRecipe().getIngredientsWithMeasurements() != null
                        && !post.getRecipe().getIngredientsWithMeasurements().isEmpty())
                .filter(post -> post.getRecipe().getIngredientsWithMeasurements().stream()
                        .allMatch(ingredient -> ingredientNamesLower.contains(
                                ingredient.getName().toLowerCase())))
                .map(post -> new PostWithMatchCount(post,
                        (int) post.getRecipe().getIngredientsWithMeasurements().stream()
                                .filter(ingredient -> ingredientNamesLower.contains(
                                        ingredient.getName().toLowerCase()))
                                .count()))
                .sorted(Comparator.comparingInt(PostWithMatchCount::matchCount).reversed())
                .map(PostWithMatchCount::post)
                .toList();

        return filteredPosts.stream()
                .map(post -> convertToDto(post, sessionToken))
                .toList();
    }

    public PageDto<PostDto> searchPostsByTagName(String tagId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostPhotoView> postPage = postRepository.findPostsByTagsTagId(tagId, pageable);

        return getPostDtoPageDtoFromPostPhotoView(postPage, pageable);
    }

    @Cacheable(value = "userPosts", key = "#userId + '_' + #page + '_' + #size")
    public PageDto<PostDto> getUserPosts(String userId, Integer page, Integer size) {
        userService.checkIfUserExists(userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<PostPhotoView> postsPhotoViewPage = postRepository.findPostsByUserId(userId, pageable);

        return getPostDtoPageDtoFromPostPhotoView(postsPhotoViewPage, pageable);
    }


    public PageDto<PostDto> getPostDtoPageDto(List<Post> posts, Long total, Pageable pageable, String sessionToken) {
        List<PostDto> postDtos = posts.stream()
                .map(post -> convertToDto(post, sessionToken))
                .toList();

        PageImpl<PostDto> pageImpl = new PageImpl<>(postDtos, pageable, total);

        return getPageDto(pageImpl);
    }

    public PageDto<PostDto> getPostDtoPageDtoFromPostPhotoView(Page<PostPhotoView> postsPhotoViewPage, Pageable pageable) {
        List<PostDto> postDtos = postsPhotoViewPage.stream().map(post -> {
            PostDto postDto = new PostDto();
            postDto.setPostId(post.getPostId());
            postDto.setPostMedia(post.getPostMedia());
            return postDto;
        }).toList();

        PageImpl<PostDto> pageImpl = new PageImpl<>(postDtos, pageable, postsPhotoViewPage.getTotalElements());

        return getPageDto(pageImpl);
    }

    public PageDto<PostDto> getPageDto(PageImpl<PostDto> pageImpl) {
        PageDto<PostDto> pageDto = new PageDto<>();
        pageDto.setContent(pageImpl.getContent());
        pageDto.setPageNumber(pageImpl.getNumber());
        pageDto.setPageSize(pageImpl.getSize());
        pageDto.setTotalElements(pageImpl.getTotalElements());
        pageDto.setTotalPages(pageImpl.getTotalPages());

        return pageDto;
    }

    public Recipe getPostRecipe(String postId) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new ResourceNotFoundException("Post with id " + postId + " not found"));
        return post.getRecipe();
    }

    @Cacheable(value = "likedPosts", key = "#userId + '_' + #sessionToken")
    public List<PostDto> getPostsLikedByUser(String userId, String sessionToken) {
        var likes = likeRepository.findByUserId(userId);
        var posts = postRepository.findByLikesIn(likes);

        return posts.stream()
                .map(post -> convertToDto(post, sessionToken))
                .toList();
    }

    @CacheEvict(value = {"posts", "postById", "userPosts", "postsByTag", "likedPosts", "postsAll"}, allEntries = true)
    public PostDto createPost(PostDto postDto, String sessionToken) {
        UserShort currentUser = userService.getCurrentUserShortBySessionToken(sessionToken);

        Post post = convertToEntity(postDto);

        if (post.getUserId() == null) throw new NoSuchElementException("Post User ID cannot be null");
        if (post.getRecipe() == null) throw new NoSuchElementException("Post Recipe ID cannot be null");

        post.setUserId(currentUser.getUserId());

        Post savedPost = postRepository.save(post);

        PostDto responseDto = modelMapper.map(savedPost, PostDto.class);
        responseDto.setLikesCount(0L);
        responseDto.setCommentsCount(0L);
        responseDto.setLikedByCurrentUser(false);

        PostAuthorDto authorDto = convertToPostAuthorDto(currentUser);
        responseDto.setPostAuthorDto(authorDto);
        return responseDto;
    }

    public PostDto convertToDto(Post post, String sessionToken) {
        PostDto postDto = modelMapper.map(post, PostDto.class);
        postDto.setLikesCount((long) post.getLikes().size());
        postDto.setCommentsCount((long) post.getComments().size());

        UserShort currentUser = userService.getCurrentUserShortBySessionToken(sessionToken);
        var like = likeRepository.findByPostIdAndUserId(post.getPostId(), currentUser.getUserId());
        postDto.setLikedByCurrentUser(like.isPresent());

        UserShort author = userService.findUserShortByUserId(post.getUserId());
        PostAuthorDto postAuthorDto = convertToPostAuthorDto(author);
        postDto.setPostAuthorDto(postAuthorDto);

        return postDto;
    }

    public PageDto<PostDto> getPostsExcludingIngredients(List<String> ingredientNames, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostPhotoView> postPage = postRepository.findByExcludedIngredients(ingredientNames,
                pageable);

        return getPostDtoPageDtoFromPostPhotoView(postPage, pageable);
    }

    private Post convertToEntity(PostDto postDto) {
        Post post = modelMapper.map(postDto, Post.class);
        if (postDto.getPostAuthorDto() != null) {
            post.setUserId(postDto.getPostAuthorDto().getUserId());
        }
        return post;
    }

    public PageDto<PostDto> convertPostsToPageDto(String sessionToken, List<Post> posts,
                                                  Pageable pageable) {
        Map<String, UserShort> userShortMap = getAuthors(posts);

        List<PostDto> postDtos = posts.stream()
                .map(post -> {
                    PostDto postDto = convertToDto(post, sessionToken);
                    addAuthorInfo(post, userShortMap, postDto);
                    return postDto;
                })
                .toList();

        PageImpl<PostDto> pageImpl = new PageImpl<>(postDtos, pageable, postDtos.size());
        return getPageDto(pageImpl);
    }

    private Map<String, UserShort> getAuthors(List<Post> posts) {
        List<String> userIds = posts.stream()
                .map(Post::getUserId)
                .distinct()
                .toList();

        List<UserShort> userShorts = userService.getUserShortByIdIn(userIds);

        return userShorts.stream()
                .collect(Collectors.toMap(UserShort::getUserId, userShort -> userShort));
    }

    private void addAuthorInfo(Post post, Map<String, UserShort> userShortMap, PostDto postDto) {
        UserShort userShort = userShortMap.get(post.getUserId());
        if (userShort != null) {
            postDto.setPostAuthorDto(convertToPostAuthorDto(userShort));
        }
    }

    private PostAuthorDto convertToPostAuthorDto(UserShort userShort) {
        PostAuthorDto postAuthorDto = new PostAuthorDto();
        postAuthorDto.setUserId(userShort.getUserId());
        postAuthorDto.setDisplayName(userShort.getDisplayName());
        postAuthorDto.setProfilePicture(userShort.getProfilePicture());
        return postAuthorDto;
    }
}