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
import pl.jakubkonkol.tasteitserver.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;
    private final MongoTemplate mongoTemplate;

    public void save (Post post) {
        if (post == null) {
            throw new IllegalArgumentException("Post cannot be null.");
        }
        postRepository.save(post);
    }

    public void saveAll (List<Post> posts) {
        if (posts == null) {
            throw new IllegalArgumentException("List of posts cannot be null.");
        }
        postRepository.saveAll(posts);
    }

    public void deleteAll() {
        postRepository.deleteAll();
    }

    public List<Post> getAll() {
        return postRepository.findAll();
    }

    public PostDto getPost(String postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NoSuchElementException("Post with id " + postId + " not found"));
        return convertToDto(post);
    }

    //temp implementation
    public PageDto<PostDto> getRandomPosts(Integer page, Integer size) {
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
                .map(this::convertToDto)
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
    public List<PostDto> searchPostsByTitle(String query) {
        List<Post> posts = postRepository.findByPostMediaTitleContainingIgnoreCase(query);
        if(posts.isEmpty()){
            //raczej nic nie trzeba
        }
        return posts.stream()
                .map(this::convertToDto)
                .toList();
    }

    public Recipe getPostRecipe(String postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NoSuchElementException("Post with id " + postId + " not found"));
        return post.getRecipe();
    }

    private PostDto convertToDto(Post post) {
        return modelMapper.map(post, PostDto.class);
    }
}
