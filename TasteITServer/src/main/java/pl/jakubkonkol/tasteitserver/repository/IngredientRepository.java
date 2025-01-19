package pl.jakubkonkol.tasteitserver.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import pl.jakubkonkol.tasteitserver.model.Ingredient;
import org.springframework.data.mongodb.repository.MongoRepository;
import pl.jakubkonkol.tasteitserver.model.projection.IngredientSearchView;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface IngredientRepository extends MongoRepository<Ingredient, String> {
    Optional<Ingredient> findByName(String name);
    Optional<Ingredient> findByNameIgnoreCase(String name);

    @Query(value = "{ 'name': { $regex: ?0, $options: 'i' } }", fields = "{ 'ingredientId': 1, 'name': 1 }")
    Page<IngredientSearchView> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Collection<Ingredient> findByNameIgnoreCaseIn(Set<String> newNames);
}
