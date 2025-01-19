package pl.jakubkonkol.tasteitserver.service.interfaces;

public interface IPostValidationService {
    void deletePost(String postId, String sessionToken);
}
