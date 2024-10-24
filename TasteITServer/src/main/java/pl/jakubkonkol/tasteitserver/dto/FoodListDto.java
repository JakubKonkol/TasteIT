package pl.jakubkonkol.tasteitserver.dto;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import pl.jakubkonkol.tasteitserver.model.Post;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class FoodListDto {
    public String foodListId;
    public String name;
    public Date createdDate;
    public List<Post> postsList = new ArrayList<>();;
}
