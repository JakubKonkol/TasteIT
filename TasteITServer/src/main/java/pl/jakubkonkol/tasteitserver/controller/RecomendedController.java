package pl.jakubkonkol.tasteitserver.controller;

import cc.mallet.topics.ParallelTopicModel;
import org.springframework.web.bind.annotation.*;
import pl.jakubkonkol.tasteitserver.model.LDAModel;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.repository.PostRepository;
import pl.jakubkonkol.tasteitserver.repository.UserRepository;
import pl.jakubkonkol.tasteitserver.service.LdaService;
import pl.jakubkonkol.tasteitserver.service.PostScoringService;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/recomended")
public class RecomendedController {
    private final PostScoringService scoringService;
    private final LdaService ldaService;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public RecomendedController(PostScoringService scoringService, LdaService ldaService, PostRepository postRepository, UserRepository userRepository) {
        this.scoringService = scoringService;
        this.ldaService = ldaService;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public List<Post> getRecommendedPosts(@RequestParam String userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Pobieranie wszystkich postów
        List<Post> posts = postRepository.findAll();

        // Pobierz model LDA z bazy danych lub innego źródła
        LDAModel ldaModel = ldaModelRepository.findByModelName("Default_LDA_Model")
                .orElseThrow(() -> new RuntimeException("LDA Model not found"));

        ParallelTopicModel model = loadLdaModel(ldaModel); // Metoda ładująca model z bazy danych

        // Sortowanie postów według wyniku oceny
        return posts.stream()
                .sorted(Comparator.comparingDouble(post -> scoringService.calculatePostScore(post, user, model)))
                .collect(Collectors.toList());
    }


    @PostMapping("/lda/train")
    public void trainLdaModel() throws IOException {
        List<Post> posts = postRepository.findAll();
        ldaService.trainLdaModel(posts);
    }
}

