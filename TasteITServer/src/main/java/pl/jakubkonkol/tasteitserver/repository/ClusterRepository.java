package pl.jakubkonkol.tasteitserver.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import pl.jakubkonkol.tasteitserver.model.Cluster;
import pl.jakubkonkol.tasteitserver.model.enums.ClusterStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ClusterRepository extends MongoRepository<Cluster, String> {
    Optional<Cluster> findByClusterId(String clusterId);
    List<Cluster> findByClusterIdIn(Collection<String> clusterIds);
    List<Cluster> findByStatus(ClusterStatus status);

    @Query("{'status': 'LEGACY', 'lastUsedDate': { $lt: ?0 }}")
    List<Cluster> findUnusedLegacyClusters(LocalDateTime cutoffDate);

    @Query("{'status': 'LEGACY', 'usageCount': { $lt: ?0 }}")
    List<Cluster> findLowUsageClusters(int usageThreshold);
}
