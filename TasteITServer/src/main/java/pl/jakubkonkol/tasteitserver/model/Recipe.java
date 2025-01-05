package pl.jakubkonkol.tasteitserver.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pl.jakubkonkol.tasteitserver.dto.IngredientDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Recipe {
    @NotEmpty(message = "Steps cannot be empty")
    private Map<Integer,  @NotNull(message = "Step Value cannot be null") @NotBlank(message = "Step Value cannot be blank.") String> steps = new HashMap<>();
    private Map<Integer, String> pictures = new HashMap<>();
    @NotEmpty(message = "At least one ingredient is required.")
    private List<IngredientWrapper> ingredientsWithMeasurements = new ArrayList<>();
}
