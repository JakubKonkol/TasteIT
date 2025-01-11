package pl.jakubkonkol.tasteitserver.service;

import org.jetbrains.annotations.NotNull;
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
import pl.jakubkonkol.tasteitserver.dto.PageDto;
import pl.jakubkonkol.tasteitserver.dto.PostAuthorDto;
import pl.jakubkonkol.tasteitserver.dto.PostDto;
import pl.jakubkonkol.tasteitserver.dto.UserReturnDto;
import pl.jakubkonkol.tasteitserver.exception.ResourceNotFoundException;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.Recipe;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.model.enums.PostType;
import pl.jakubkonkol.tasteitserver.model.projection.PostPhotoView;
import pl.jakubkonkol.tasteitserver.model.projection.UserShort;
import pl.jakubkonkol.tasteitserver.repository.CommentRepository;
import pl.jakubkonkol.tasteitserver.repository.LikeRepository;
import pl.jakubkonkol.tasteitserver.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.repository.UserRepository;
import pl.jakubkonkol.tasteitserver.service.interfaces.IPostService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IUserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService implements IPostService {
    private final MongoTemplate mongoTemplate;
    private final ModelMapper modelMapper;
    private final PostRepository postRepository;
    
    @Lazy
    @Autowired
    private UserService userService;
    
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @CacheEvict(value = {"posts", "postById", "userPosts", "postsByTag", "likedPosts", "postsAll"}, allEntries = true)
    public void save(PostDto postDto) {
        Post post = convertToEntity(postDto);
        postRepository.save(Objects.requireNonNull(post, "Post cannot be null."));
    }

    @CacheEvict(value = {"posts", "postById", "userPosts", "postsByTag", "likedPosts", "postsAll"}, allEntries = true)
    public List<Post> saveAll(List<Post> posts) {
        return postRepository.saveAll(Objects.requireNonNull(posts, "List of posts cannot be null."));
    }

    @CacheEvict(value = {"posts", "postById", "userPosts", "postsByTag", "likedPosts", "postsAll"}, allEntries = true)
    public void deleteAll() {
        postRepository.deleteAll();
    }

    @Cacheable(value = "postsAll", key = "'AllPosts'")
    public List<Post> getAll() {
        return postRepository.findAll();
    }

    @Cacheable(value = "postById", key = "#postId")
    public PostDto getPost(String postId, String sessionToken) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post with id " + postId + " not found"));

        PostDto postDto = convertToDto(post, sessionToken);
        PostAuthorDto postAuthorDto = new PostAuthorDto();

        UserShort userShort = userService.findUserShortByUserId(post.getUserId());
        postAuthorDto.setUserId(userShort.getUserId());
        postAuthorDto.setDisplayName(userShort.getDisplayName());
        postAuthorDto.setProfilePicture(userShort.getProfilePicture());
        postDto.setPostAuthorDto(postAuthorDto);

        return postDto;
    }

    //temp implementation

    public PageDto<PostDto> getRandomPosts(Integer page, Integer size, String sessionToken) {
        Pageable pageable = PageRequest.of(page, size);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.sample(size)
        );

        AggregationResults<Post> results = mongoTemplate.aggregate(aggregation, "post", Post.class);
        List<Post> posts = results.getMappedResults();

        if (posts.isEmpty()) {
            throw new NoSuchElementException("No random posts found in repository");
        }

        List<String> userIds = posts.stream()
                .map(Post::getUserId)
                .distinct()
                .toList();

        List<UserShort> userShorts = userService.getUserShortByIdIn(userIds);

        Map<String, UserShort> userShortMap = userShorts.stream()
                .collect(Collectors.toMap(UserShort::getUserId, userShort -> userShort));

        List<PostDto> postDtos = posts.stream()
                .map(post -> {
                    PostDto postDto = convertToDto(post, sessionToken);
                    UserShort userShort = userShortMap.get(post.getUserId());
                    if (userShort != null) {
                        PostAuthorDto postAuthorDto = new PostAuthorDto();
                        postAuthorDto.setUserId(userShort.getUserId());
                        postAuthorDto.setDisplayName(userShort.getDisplayName());
                        postAuthorDto.setProfilePicture(userShort.getProfilePicture());
                        postDto.setPostAuthorDto(postAuthorDto);
                    }
                    return postDto;
                })
                .toList();

        PageImpl<PostDto> pageImpl = new PageImpl<>(postDtos, pageable, postDtos.size());
        return getPageDto(pageImpl);
    }

    //if title consists few words use '%20' between them in get request
    public PageDto<PostDto> searchPosts(String title, String postType, String sessionToken, int page, int size) {
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

    @Cacheable(value = "postsByTag", key = "#tagId + '_' + #page + '_' + #size")
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
        Post post = postRepository.findById(postId).orElseThrow(() -> new NoSuchElementException("Post with id " + postId + " not found"));
        return post.getRecipe();
    }

    @Cacheable(value = "likedPosts", key = "#userId + '_' + #sessionToken")
    public List<PostDto> getPostsLikedByUser(String userId, String sessionToken) {
        var likes = likeRepository.findByUserId(userId);
        var posts = postRepository.findByLikesIn(likes);

        return posts.stream()
                .map(post->convertToDto(post, sessionToken))
                .toList();
    }

    @CacheEvict(value = {"posts", "postById", "userPosts", "postsByTag", "likedPosts", "postsAll"}, allEntries = true)
    public PostDto createPost(PostDto postDto, String sessionToken) {
        Post post = convertToEntity(postDto);
        postRepository.save(post);
        return convertToDto(post, sessionToken);
    }

    public PostDto convertToDto(Post post, String sessionToken) {
        PostDto postDto = modelMapper.map(post, PostDto.class);
        postDto.setLikesCount((long) post.getLikes().size());
        postDto.setCommentsCount((long) post.getComments().size());

        var currentUser = userService.getCurrentUserDtoBySessionToken(sessionToken); //todo optymalizacja dla wielu postow
        var like = likeRepository.findByPostIdAndUserId(post.getPostId(), currentUser.getUserId()); //todo optymalizacja dla wielu postow

        if(like.isEmpty()){
            postDto.setLikedByCurrentUser(false);
            // nie wiem czy ustawialbym to pole w tym miejscu moze np w getRandomPosts i getPost? jest to jednak metoda konkretnie do konwersji
            //" Aby wiedzieć, czy dany użytkownik jest obserwowany przez innego użytkownika,
            // potrzebujesz dodatkowego kontekstu, czyli danych o użytkowniku aktualnie zalogowanym (np. currentUser).
            // Ta informacja nie powinna być dostępna bezpośrednio w metodzie konwertującej."
        }
        else {
            postDto.setLikedByCurrentUser(true);
        }
        return postDto;
    }

    public PageDto<PostDto> getPostsExcludingIngredients(List<String> ingredientNames, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostPhotoView> postPage = postRepository.findByExcludedIngredients(ingredientNames, pageable);

        return getPostDtoPageDtoFromPostPhotoView(postPage, pageable);
    }

    private Post convertToEntity(PostDto postDto) {
        return modelMapper.map(postDto, Post.class);
    }

    public PageDto<PostDto> convertPostsToPageDto(String sessionToken, List<Post> posts, Pageable pageable) {
        List<String> userIds = posts.stream()
                .map(Post::getUserId)
                .distinct()
                .toList();

        List<UserShort> userShorts = userService.getUserShortByIdIn(userIds);

        Map<String, UserShort> userShortMap = userShorts.stream()
                .collect(Collectors.toMap(UserShort::getUserId, userShort -> userShort));

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

    private void addAuthorInfo(Post post, Map<String, UserShort> userShortMap, PostDto postDto) {
        UserShort userShort = userShortMap.get(post.getUserId());
        if (userShort != null) {
            PostAuthorDto postAuthorDto = new PostAuthorDto();
            postAuthorDto.setUserId(userShort.getUserId());
            postAuthorDto.setDisplayName(userShort.getDisplayName());
            postAuthorDto.setProfilePicture(userShort.getProfilePicture());
            postDto.setPostAuthorDto(postAuthorDto);
        }
    }

    @Override
    @Transactional
    public void deletePost(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post with id " + postId + " not found"));

        // Usuń wszystkie komentarze posta
        post.getComments().forEach(comment -> 
            commentRepository.deleteById(comment.getCommentId())
        );

        // Usuń wszystkie polubienia posta
        post.getLikes().forEach(like -> 
            likeRepository.deleteById(like.getLikeId())
        );

        // Usuń post z listy postów użytkownika
        User user = userRepository.findById(post.getUserId())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        user.getPosts().removeIf(p -> p.getPostId().equals(postId));
        userRepository.save(user);

        // Na końcu usuń sam post
        postRepository.deleteById(postId);
    }
}