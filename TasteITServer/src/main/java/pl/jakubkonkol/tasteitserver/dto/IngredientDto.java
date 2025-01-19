package pl.jakubkonkol.tasteitserver.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class IngredientDto {
    private String ingredientId;
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 1, max = 80, message = "Name must be between 1 and 80 characters")
    private String name;
    @Size(max = 500, message = "Description cannot be longer than 500 characters")
    private String description;
    private String type;
    private boolean isAlcohol;
    private String strength;
    private String imageURL;
}