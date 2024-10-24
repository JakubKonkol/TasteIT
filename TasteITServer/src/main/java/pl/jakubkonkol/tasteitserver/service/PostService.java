package pl.jakubkonkol.tasteitserver.service;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import pl.jakubkonkol.tasteitserver.dto.PageDto;
import pl.jakubkonkol.tasteitserver.dto.PostDto;
import pl.jakubkonkol.tasteitserver.exception.ResourceNotFoundException;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.Recipe;
import pl.jakubkonkol.tasteitserver.model.Tag;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.repository.LikeRepository;
import pl.jakubkonkol.tasteitserver.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PostService {
    private final UserService userService;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
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
    }

    public List<Post> getAll() {
        return postRepository.findAll();
    }

    public PostDto getPost(String postId, String sessionToken) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post with id " + postId + " not found"));
        return convertToDto(post, sessionToken);
    }

    //temp implementation
    public PageDto<PostDto> getRandomPosts(Integer page, Integer size, String sessionToken) {
        // Create a Pageable object for pagination information
        Pageable pageable = PageRequest.of(page, size);

        // Count the total number of posts
        long total = postRepository.count();

        // Create an aggregation to sample random documents from the collection
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.sample(size)
        );

        // Execute the aggregation query
        AggregationResults<Post> results = mongoTemplate.aggregate(aggregation, "post", Post.class);
        List<Post> posts = results.getMappedResults();
        if(posts.isEmpty()){
            throw new NoSuchElementException("No random posts found in repository");
        }

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

    //if title consists few words use '%20' between them in get request
    public List<PostDto> searchPostsByTitle(String query, String sessionToken) {
        List<Post> posts = postRepository.findByPostMediaTitleContainingIgnoreCase(query);
        if (posts.isEmpty()){
            //raczej nic nie trzeba
        }
        return posts.stream()
                .map(post -> convertToDto(post, sessionToken))
                .toList();
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
        postRepository.save(post);
        return convertToDto(post, sessionToken);
    }

    private PostDto convertToDto(Post post, String sessionToken) {
        PostDto postDto = modelMapper.map(post, PostDto.class);
        postDto.setLikesCount((long) post.getLikes().size());
        postDto.setCommentsCount((long) post.getComments().size());

        var currentUser = userService.getCurrentUserBySessionToken(sessionToken); //todo optymalizacja dla wielu postow
        var like = likeRepository.findByPostIdAndUserId(post.getPostId(), currentUser.getUserId()); //todo optymalizacja dla wielu postow

        if(like.isEmpty()){
            postDto.setLikedByCurrentUser(false);
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