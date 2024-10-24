package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.Tag;
import pl.jakubkonkol.tasteitserver.repository.PostRepository;
import pl.jakubkonkol.tasteitserver.repository.TagRepository;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
//    private final PostRepository postRepository;

    public void saveAll(List<Tag> tags) {
        tagRepository.saveAll(Objects.requireNonNull(tags, "Tag cannot be null"));
    }

    public List<Tag> getAll (){
        return tagRepository.findAll();
    }

//    public void addTagsListToPost(List<Tag> tags, String postId) {
//        Post post = postRepository.findById(postId).orElseThrow();
//        post.setTags(tags);
//    }



}
