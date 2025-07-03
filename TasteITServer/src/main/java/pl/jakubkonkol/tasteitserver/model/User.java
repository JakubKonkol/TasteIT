package pl.jakubkonkol.tasteitserver.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.jakubkonkol.tasteitserver.model.enums.PostType;

import java.util.*;

@Document
@Data
public class User implements UserDetails {
    @Id
    private String userId;
    private String email;
    private String displayName = "guest";
    private String bio = "";
    private String profilePicture = "https://github.com/JakubKonkol/TasteIT/blob/master/assets/guest.png";
    @CreatedDate
    private Date createdAt;
    private Date birthDate = new Date();
    private Authentication authentication;
    private Boolean firstLogin = true;
    private List<String> roles = List.of("USER");
    @DBRef
    private List<Tag> tags = new ArrayList<>();
    @DBRef
    private List<Tag> bannedTags = new ArrayList<>();
    @DBRef
    private List<Ingredient> bannedIngredients = new ArrayList<>();
    private List<String> followers = new ArrayList<>();
    private List<String> following = new ArrayList<>();
    private List<FoodList> foodLists = new ArrayList<>();

//    @DBRef
    private List<Post> posts = new ArrayList<>();   //ustawiac przy budowaniu bazy
  
    private Map<String, Double> clusterPreferences = new HashMap<>();
    @DBRef
    private List<Badge> earnedBadges = new ArrayList<>();
    private List<Comment> createdComments = new ArrayList<>();


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(r -> (GrantedAuthority) () -> r).toList();
    }

    @Override
    public String getPassword() {
        return authentication.getPassword();
    }

    @Override
    public String getUsername() {
        return email;
    }


    public List<Recipe> getRecipes() {
        return posts.stream()
                .map(Post::getRecipe)
                .toList();
    }

    public void addEarnedBadge(Badge badge) {
        earnedBadges.add(badge);
    }

    public List<Post> getPostsBy(PostType type){
        return posts.stream()
                .filter(post -> post.getPostType() == type)
                .toList();
    }

    public Optional<Post> getPostWithMaxLikes(){
        return posts.stream()
                .max(Comparator.comparingInt(post -> post.getLikes().size()));
    }

    public int countAllLikesOnPosts() {
        return posts.stream()
                .mapToInt(post -> post.getLikes().size())
                .sum();
    }

    public int countPostsInFoodlists(){
        return foodLists.stream()
                .mapToInt(foodlist -> foodlist.getPostsList().size())
                .sum();
    }

    public Optional<Badge> getEarnedBadgeBy(int id) {
        return earnedBadges.stream()
                .filter(badge -> badge.getId() == id)
                .findAny();
    }
}

