package pl.jakubkonkol.tasteitserver.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Document(collection = "lda_models")
@Data
@Builder
public class LDAModel {
    @Id
    private String modelId; // Identyfikator modelu
    private String modelName; // Opcjonalna nazwa modelu
    private int numTopics; // Liczba tematów
    private Map<Integer, List<String>> topicWords; // Lista słów dla każdego tematu
    private Date createdDate; // Data utworzenia modelu
}

