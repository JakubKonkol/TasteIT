package mixitserver.repository;

import mixitserver.model.domain.Drink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrinkRepository extends JpaRepository<Drink, Integer> {
    @Query("SELECT drink FROM Drink drink WHERE " +
            "(:category IS NULL OR drink.category = :category) " +
            "AND (:isAlcoholic IS NULL OR drink.isAlcoholic = :isAlcoholic) " +
            "AND (:glassType IS NULL OR drink.glassType = :glassType)")
    List<Drink> filterDrinks(@Param("category")String category, @Param("isAlcoholic") Boolean isAlcoholic, @Param("glassType") String glassType);
    List<Drink> findTop10ByOrderByPopularityDesc();
    //List<Drink> findAllByNameOrderByPopularityDesc(String drinkName);
//    List<Drink> findByNameContainingOrderByPopularityDesc(String drinkName);
    List<Drink> findByNameContainingIgnoreCaseOrderByPopularityDesc(String drinkName);

}
