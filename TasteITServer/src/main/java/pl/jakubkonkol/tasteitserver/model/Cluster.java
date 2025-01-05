package pl.jakubkonkol.tasteitserver.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import pl.jakubkonkol.tasteitserver.model.enums.ClusterStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document
@Data
public class Cluster {
    @Id
    private String id;
    @Indexed(unique = true)
    @JsonAlias({"cluster_id"})
    private String clusterId;
    private String name;
    @JsonAlias({"main_topics"})
    private List<String> mainTopics = new ArrayList<>();
    @JsonAlias({"keyword_weights"})
    private Map<String, Double> keywordWeights = new HashMap<>();
    private ClusterStatus status = ClusterStatus.ACTIVE;
    private LocalDateTime lastUsedDate = LocalDateTime.now();
    private LocalDateTime createdDate = LocalDateTime.now();
    private int usageCount = 0;
}
