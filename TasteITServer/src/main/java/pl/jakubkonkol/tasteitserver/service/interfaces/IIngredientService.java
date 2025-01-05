package pl.jakubkonkol.tasteitserver.service.interfaces;

import pl.jakubkonkol.tasteitserver.dto.IngredientDto;
import pl.jakubkonkol.tasteitserver.dto.PageDto;
import pl.jakubkonkol.tasteitserver.model.Ingredient;
import pl.jakubkonkol.tasteitserver.model.IngredientWrapper;

import java.util.List;
import java.util.Optional;

public interface IIngredientService {
    Optional<Ingredient> findByName(String name);
    IngredientDto getIngredient(String ingredientId);
    IngredientDto save(Ingredient ingredient);
    List<IngredientDto> saveAll(List<Ingredient> ingredients);
    void deleteAll();
    void deleteById(String ingredientId);
    List<IngredientDto> getAll();
    PageDto<IngredientDto> searchIngredientsByName(String name, Integer page, Integer size);
    IngredientDto convertToDto(Ingredient ingredient);
    IngredientWrapper convertToWrapper(Ingredient ingredient);
    Ingredient convertToEntity(IngredientDto ingredientDto);
}
