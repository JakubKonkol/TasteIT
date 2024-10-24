package pl.jakubkonkol.tasteitserver.service;

import com.sun.jdi.IntegerValue;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
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
import pl.jakubkonkol.tasteitserver.dto.PostDto;
import pl.jakubkonkol.tasteitserver.dto.UserReturnDto;
import pl.jakubkonkol.tasteitserver.exception.ResourceNotFoundException;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.Recipe;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.model.enums.PostType;
import pl.jakubkonkol.tasteitserver.repository.LikeRepository;
import pl.jakubkonkol.tasteitserver.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PostService {
    private final UserService userService;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final MongoTemplate mongoTemplate;

    public void save(PostDto postDto) {
        Post post = convertToEntity(postDto);
        postRepository.save(Objects.requireNonNull(post, "Post cannot be null."));
    }

    public void saveAll(List<Post> posts) {
        postRepository.saveAll(Objects.requireNonNull(posts, "List of posts cannot be null."));
    }

    public void deleteAll() {
        postRepository.deleteAll();
    } //TODO niebezpieczne

    public List<Post> getAll() {
        return postRepository.findAll();
    }

    public PostDto getPost(String postId, String sessionToken) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post with id " + postId + " not found"));
        return convertToDto(post, sessionToken);
    }

    public long countPostsByUserId(String userId) {
        return postRepository.countByUserId(userId);
    }

    //temp implementation
    public PageDto<PostDto> getRandomPosts(Integer page, Integer size, String sessionToken) {
        Pageable pageable = PageRequest.of(page, size);

        long total = postRepository.count();

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.sample(size)
        );

        AggregationResults<Post> results = mongoTemplate.aggregate(aggregation, "post", Post.class);
        List<Post> posts = results.getMappedResults();

        if (posts.isEmpty()) {
            throw new NoSuchElementException("No random posts found in repository");
        }

        return getPostDtoPageDto(posts, total, pageable, sessionToken);
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

    public PageDto<PostDto> searchPostsByTagName(String tagName, String sessionToken, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findByTagNameIgnoreCase(tagName, pageable);

        return getPostDtoPageDto(postPage.getContent(), postPage.getTotalElements(), pageable, sessionToken);
    }

    private PageDto<PostDto> getPostDtoPageDto(List<Post> posts, long total, Pageable pageable, String sessionToken) {
        List<PostDto> postDtos = posts.stream()
                .map(post -> convertToDto(post, sessionToken))
                .toList();

        PageImpl<PostDto> pageImpl = new PageImpl<>(postDtos, pageable, total);

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

    public List<PostDto> getPostsLikedByUser(String userId, String sessionToken) {
        var likes = likeRepository.findByUserId(userId);
        var posts = postRepository.findByLikesIn(likes);

        return posts.stream()
                .map(post->convertToDto(post, sessionToken))
                .toList();
    }

    public PostDto createPost(PostDto postDto, String sessionToken) {
        Post post = convertToEntity(postDto);
//        User postOwner = userRepository.findById(post.getUserId()).orElseThrow();
//        int postsCount = postOwner.getPostsCount() + 1;
//        postOwner.setPostsCount(postsCount);
        postRepository.save(post);
        return convertToDto(post, sessionToken);
    }

    private PostDto convertToDto(Post post, String sessionToken) {
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

    private Post convertToEntity(PostDto postDto) {
        return modelMapper.map(postDto, Post.class);
    }
}