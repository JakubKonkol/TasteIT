package pl.jakubkonkol.tasteitserver.service.interfaces;

import java.util.Map;

public interface IUserPreferencesAnalysisService {
    void requestPreferenceAnalysis(String userId);
    void handlePreferenceAnalysisResponse(Map<String, Object> response);
}
