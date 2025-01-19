package pl.jakubkonkol.tasteitserver.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Document
@Data
public class FoodList {
    @Id
    private String foodListId = UUID.randomUUID().toString();;
    private String name;
    @CreatedDate
    private Date createdDate = new Date();
    @DBRef
    private List<Post> postsList = new ArrayList<>();;
}
