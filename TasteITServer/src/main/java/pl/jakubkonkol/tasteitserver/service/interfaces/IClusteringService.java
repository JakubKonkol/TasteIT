package pl.jakubkonkol.tasteitserver.service.interfaces;

import org.springframework.messaging.handler.annotation.Payload;

import java.util.Map;

public interface IClusteringService {
    void requestClustering();
    void handleClusteringResponse(@Payload Map<String, Object> response);
}
