package pl.jakubkonkol.tasteitserver.model;

import lombok.Data;

@Data
public class IngredientWrapper {
    private String ingredientId;
    private String name;
    private String description;
    private String type;
    private boolean isAlcohol;
    private String strength;
    private String imageURL;
    private Measurement measurement;
}