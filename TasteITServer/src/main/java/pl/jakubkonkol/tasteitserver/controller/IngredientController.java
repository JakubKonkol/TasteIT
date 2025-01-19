package pl.jakubkonkol.tasteitserver.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubkonkol.tasteitserver.dto.IngredientDto;
import pl.jakubkonkol.tasteitserver.model.GenericResponse;
import pl.jakubkonkol.tasteitserver.service.interfaces.IIngredientService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ingredient")
@RequiredArgsConstructor
public class IngredientController {
    private final IIngredientService ingredientService;

    @PostMapping("/")
    public ResponseEntity<IngredientDto> save(@Valid @RequestBody IngredientDto ingredient) {
        var ingredientDto = ingredientService.save(ingredient);
        return ResponseEntity.ok(ingredientDto);
    }

    @PostMapping("/save-all")
    public ResponseEntity<List<IngredientDto>> saveAll(
            @RequestBody @NotEmpty(message = "Ingredients list cannot be empty") List<@Valid IngredientDto> ingredients
    ) {
        var ingredientDtoList = ingredientService.saveAll(ingredients);
        return ResponseEntity.ok(ingredientDtoList);
    }

    @DeleteMapping("/{ingredientId}")
    public ResponseEntity<GenericResponse> deleteById(@PathVariable String ingredientId) {
        ingredientService.deleteById(ingredientId);
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value())
                .message("Ingredient deleted successfully")
                .build());
    }

    @DeleteMapping("/")
    public ResponseEntity<GenericResponse> deleteAll() {
        ingredientService.deleteAll();
        return ResponseEntity.ok(GenericResponse
                .builder()
                .status(HttpStatus.OK.value())
                .message("Ingredients deleted successfully")
                .build());
    }

    @GetMapping("/{ingredientId}")
    public ResponseEntity<IngredientDto> getIngredient(@PathVariable String ingredientId) {
        IngredientDto ingredientDto = ingredientService.getIngredient(ingredientId);
        return ResponseEntity.ok(ingredientDto);
    }

    @GetMapping("/")
    public ResponseEntity<List<IngredientDto>> getAll() {
        return ResponseEntity.ok(ingredientService.getAll());
    }
}

