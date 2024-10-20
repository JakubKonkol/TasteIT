package pl.jakubkonkol.tasteitserver.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "foodlists")
@Data
public class FoodList {
    @Id
    private String foodListId;
    private String name;
    @CreatedDate
    private Date createdDate;
    @DBRef
    private List<Post> postsList = new ArrayList<>();;
}
