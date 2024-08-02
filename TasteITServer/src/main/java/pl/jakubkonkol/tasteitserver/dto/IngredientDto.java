package pl.jakubkonkol.tasteitserver.dto;
import lombok.Data;
@Data
public class IngredientDto {
    private String ingredientId;
    private String name;
    private String description;
    private String type;
    private boolean isAlcohol;
    private String strength;
    private String imageURL;
}
