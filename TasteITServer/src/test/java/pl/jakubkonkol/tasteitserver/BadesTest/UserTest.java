package pl.jakubkonkol.tasteitserver.BadesTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import pl.jakubkonkol.tasteitserver.model.Badge;
import pl.jakubkonkol.tasteitserver.model.FoodList;
import pl.jakubkonkol.tasteitserver.model.Post;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.model.enums.PostType;
import pl.jakubkonkol.tasteitserver.model.value.BadgeBlueprint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserTest {
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void shouldAddEarnedBadge() {
        Badge badge = newBadge(1);
        user.addEarnedBadge(badge);
        Optional<Badge> result = user.getEarnedBadgeBy(1);
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
    }

    @Test
    void shouldFindEarnedBadgeById() {
        Badge b1 = newBadge(1);
        Badge b2 = newBadge(2);
        user.addEarnedBadge(b1);
        user.addEarnedBadge(b2);
        Optional<Badge> found = user.getEarnedBadgeBy(2);
        assertTrue(found.isPresent());
        assertEquals(2, found.get().getId());
    }

    @Test
    void shouldReturnPostsByType() {
        Post foodPost = mockPost("p1", PostType.FOOD, 0);
        Post drinkPost = mockPost("p2", PostType.DRINK, 0);
        Post anotherFood = mockPost("p3", PostType.FOOD, 0);
        user.setPosts(Arrays.asList(foodPost, drinkPost, anotherFood));
        List<Post> foods = user.getPostsBy(PostType.FOOD);
        assertEquals(2, foods.size());
        assertTrue(foods.stream().allMatch(p -> p.getPostType() == PostType.FOOD));
    }

    @Test
    void shouldReturnPostWithMaxLikes() {
        Post p1 = mockPost("p1", PostType.FOOD, 3);
        Post p2 = mockPost("p2", PostType.FOOD, 7);
        Post p3 = mockPost("p3", PostType.DRINK, 2);
        user.setPosts(Arrays.asList(p1, p2, p3));
        Optional<Post> mostLiked = user.getPostWithMaxLikes();
        assertTrue(mostLiked.isPresent());
        assertEquals("p2", mostLiked.get().getPostId());
    }

    @Test
    void shouldCountAllLikesOnPosts() {
        Post p1 = mockPost("p1", PostType.FOOD, 5);
        Post p2 = mockPost("p2", PostType.DRINK, 1);
        Post p3 = mockPost("p3", PostType.FOOD, 4);
        user.setPosts(Arrays.asList(p1, p2, p3));
        int totalLikes = user.countAllLikesOnPosts();
        assertEquals(10, totalLikes);
    }

    @Test
    void shouldCountPostsInFoodlists() {
        Post p1 = mockPost("p1", PostType.FOOD, 0);
        Post p2 = mockPost("p2", PostType.FOOD, 0);
        Post p3 = mockPost("p3", PostType.DRINK, 0);
        FoodList list1 = mock(FoodList.class);
        FoodList list2 = mock(FoodList.class);
        when(list1.getPostsList()).thenReturn(Arrays.asList(p1, p2));
        when(list2.getPostsList()).thenReturn(List.of(p3));
        user.setFoodLists(Arrays.asList(list1, list2));
        int count = user.countPostsInFoodlists();
        assertEquals(3, count);
    }

    private Badge newBadge(int id) {
        BadgeBlueprint bp = mock(BadgeBlueprint.class);
        when(bp.getId()).thenReturn(id);
        return new Badge(bp);
    }

    @SuppressWarnings("unchecked")
    private Post mockPost(String id, PostType type, int likeCount) {
        Post post = mock(Post.class);
        when(post.getPostId()).thenReturn(id);
        when(post.getPostType()).thenReturn(type);
        List<Object> likesList = new ArrayList<>();
        for (int i = 0; i < likeCount; i++) {
            likesList.add(new Object());
        }
        when(post.getLikes()).thenReturn((List) likesList);
        return post;
    }
}
