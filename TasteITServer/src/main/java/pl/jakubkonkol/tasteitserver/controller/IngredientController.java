package pl.jakubkonkol.tasteitserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubkonkol.tasteitserver.dto.IngredientDto;
import pl.jakubkonkol.tasteitserver.exception.ResourceNotFoundException;
import pl.jakubkonkol.tasteitserver.model.Ingredient;
import pl.jakubkonkol.tasteitserver.service.interfaces.IIngredientService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/ingredient")
@RequiredArgsConstructor
public class IngredientController {
    private final IIngredientService ingredientService;

    @PostMapping("/")
    public ResponseEntity<IngredientDto> save(@RequestBody Ingredient ingredient) {
        var ingredientDto = ingredientService.save(ingredient);
        return ResponseEntity.ok(ingredientDto);
    }

    @PostMapping("/saveAll")
    public ResponseEntity<List<IngredientDto>> saveAll(@RequestBody List<Ingredient> ingredients) {
        var ingredientDtoList = ingredientService.saveAll(ingredients);
        return ResponseEntity.ok(ingredientDtoList);
    }

    @DeleteMapping("/{ingredientId}")
    public ResponseEntity<String> deleteById(@PathVariable String ingredientId) {
        ingredientService.deleteById(ingredientId);
        return ResponseEntity.ok("Ingredient deleted succesfully, Id: " + ingredientId);
    }

    @DeleteMapping("/")
    public ResponseEntity<String> deleteAll() {
        ingredientService.deleteAll();
        return ResponseEntity.ok("Ingredients deleted succesfully");
    }

    @GetMapping("/{ingredientId}")
    public ResponseEntity<IngredientDto> getIngredient(@PathVariable String ingredientId) {
        IngredientDto ingredientDto = ingredientService.getIngredient(ingredientId);
        if (ingredientDto == null) {
            throw new ResourceNotFoundException("Ingredient not found with Id: " + ingredientId);
        }
        return ResponseEntity.ok(ingredientDto);
    }

    @GetMapping("/")
    public ResponseEntity<List<IngredientDto>> getAll() {
        return ResponseEntity.ok(ingredientService.getAll());

    }

    @GetMapping("/findByName")
    public ResponseEntity<Ingredient> findByName(String name) {
        return ResponseEntity.ok(ingredientService.findByName(name).get());
    }
}

