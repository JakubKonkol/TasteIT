package pl.jakubkonkol.tasteitserver.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pl.jakubkonkol.tasteitserver.model.value.BadgeBlueprint;

public interface BadgeBlueprintRepository extends MongoRepository<BadgeBlueprint,Integer> {
}
