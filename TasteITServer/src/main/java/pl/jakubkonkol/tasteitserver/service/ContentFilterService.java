package pl.jakubkonkol.tasteitserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.jakubkonkol.tasteitserver.model.*;
import pl.jakubkonkol.tasteitserver.repository.UserActionRepository;
import pl.jakubkonkol.tasteitserver.service.interfaces.IContentFilterService;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentFilterService implements IContentFilterService {

    private final UserActionRepository userActionRepository;

    public List<Post> filterBannedContent(List<Post> posts, User user) {
        Set<String> interactedPostIds = getInteractedPostIds(user.getUserId());
        Set<String> bannedTagIds = getBannedTagIds(user);
        Set<String> bannedIngredientIds = getBannedIngredientIds(user);

        return posts.stream()
                .filter(post -> isPostAllowed(post, interactedPostIds, bannedTagIds, bannedIngredientIds))
                .toList();
    }

    private boolean isPostAllowed(Post post,
                                  Set<String> interactedPostIds,
                                  Set<String> bannedTagIds,
                                  Set<String> bannedIngredientIds) {

        if (interactedPostIds.contains(post.getPostId())) {
            return false;
        }

        if (post.getTags().stream()
                .map(Tag::getTagId)
                .anyMatch(bannedTagIds::contains)) {
            return false;
        }

        if (post.getRecipe() != null) {
            return post.getRecipe().getIngredientsWithMeasurements().stream()
                    .map(IngredientWrapper::getIngredientId)
                    .noneMatch(bannedIngredientIds::contains);
        }

        return true;
    }

    private Set<String> getInteractedPostIds(String userId) {
        return userActionRepository.findInteractionsByUserId(userId).stream()
                .map(action -> (String) action.getMetadata().get("postId"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<String> getBannedTagIds(User user) {
        return user.getBannedTags().stream()
                .map(Tag::getTagId)
                .collect(Collectors.toSet());
    }

    private Set<String> getBannedIngredientIds(User user) {
        return user.getBannedIngredients().stream()
                .map(Ingredient::getIngredientId)
                .collect(Collectors.toSet());
    }
}
