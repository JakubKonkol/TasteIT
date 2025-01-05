package pl.jakubkonkol.tasteitserver.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Data
@Document(collection = "userActions")
public class UserAction {
    @Id
    private String id;
    private String userId;
    private String actionType;
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;
}