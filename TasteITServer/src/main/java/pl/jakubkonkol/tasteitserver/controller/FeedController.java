package pl.jakubkonkol.tasteitserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubkonkol.tasteitserver.dto.PageDto;
import pl.jakubkonkol.tasteitserver.dto.PostDto;
import pl.jakubkonkol.tasteitserver.model.GenericResponse;
import pl.jakubkonkol.tasteitserver.service.interfaces.IClusteringService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IRankerService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IUserPreferencesAnalysisService;


@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class FeedController {
    private final IUserPreferencesAnalysisService userPreferencesAnalysisService;
    private final IRankerService rankerService;
    private final IClusteringService clusteringService;

    @GetMapping("/ranked-feed")
    public ResponseEntity<PageDto<PostDto>> getRankedFeed(@RequestParam(defaultValue = "0") Integer page,
                                                    @RequestParam(defaultValue = "20") Integer size,
                                                    @RequestHeader("Authorization") String sessionToken) {
        PageDto<PostDto> results = rankerService.rankPosts(page, size, sessionToken);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/request-clustering")
    public ResponseEntity<GenericResponse> requestClustering() {
        clusteringService.requestClustering();
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value())
                .message("Clustering requested successfully")
                .build());
    }

    @PostMapping("/analyze/{userId}")
    public ResponseEntity<GenericResponse> requestPreferenceAnalysis(@PathVariable String userId) {
        userPreferencesAnalysisService.requestPreferenceAnalysis(userId);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value())
                .message("Preference analysis requested successfully")
                .build());
    }
}
