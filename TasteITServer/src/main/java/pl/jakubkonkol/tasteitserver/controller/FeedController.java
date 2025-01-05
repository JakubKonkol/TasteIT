package pl.jakubkonkol.tasteitserver.controller;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubkonkol.tasteitserver.dto.PageDto;
import pl.jakubkonkol.tasteitserver.dto.PostDto;
import pl.jakubkonkol.tasteitserver.model.GenericResponse;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.repository.PostRepository;
import pl.jakubkonkol.tasteitserver.service.UserPreferencesAnalysisService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IClusteringService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IRankerService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IUserPreferencesAnalysisService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class FeedController {
    private final IUserPreferencesAnalysisService userPreferencesAnalysisService;
    private final IRankerService rankerService;
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;
    private final IClusteringService clusteringService;

    @GetMapping("/ranked_feed")
    public ResponseEntity<PageDto<PostDto>> getRankedFeed(@RequestParam(defaultValue = "0") Integer page,
                                                    @RequestParam(defaultValue = "20") Integer size,
                                                    @RequestHeader("Authorization") String sessionToken) {
        PageDto<PostDto> results = rankerService.rankPosts(page, size, sessionToken);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/allposts")
    public ResponseEntity<List<PostDto>> getAllPosts() {
        List<Post> all = postRepository.findAll();
        List<PostDto> list = all.stream().map(post -> modelMapper.map(post, PostDto.class)).toList();
        return ResponseEntity.ok(list);
    }
    @GetMapping("/request_clustering")
    public ResponseEntity<?> requestClustering() {
        try {
            clusteringService.requestClustering();
            return ResponseEntity.ok(Map.of("status", "Request sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to send request: " + e.getMessage()));
        }
    }

    @PostMapping("/analyze/{userId}")
    public ResponseEntity<GenericResponse> requestPreferenceAnalysis(@PathVariable String userId) {
        userPreferencesAnalysisService.requestPreferenceAnalysis(userId);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value())
                .message("Preference analysis requested")
                .build());
    }
}
