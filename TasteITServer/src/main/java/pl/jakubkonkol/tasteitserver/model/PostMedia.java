package pl.jakubkonkol.tasteitserver.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PostMedia {
    @NotBlank(message = "Title cannot be blank")
    @Size(min = 1, max = 80, message = "Title must be between 1 and 80 characters")
    private String title;
    @Size(max = 500, message = "Description cannot be longer than 500 characters")
    private String description;
    @NotEmpty(message = "At least one picture is required.")
    private List<@NotBlank(message = "Picture URL cannot be empty") String> pictures = new ArrayList<>();
}
