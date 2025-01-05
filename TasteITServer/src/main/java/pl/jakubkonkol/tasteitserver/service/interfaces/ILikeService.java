package pl.jakubkonkol.tasteitserver.service.interfaces;

public interface ILikeService {
    void likePost(String postId, String token);
    void unlikePost(String postId, String token);
    void deleteAll();
}
