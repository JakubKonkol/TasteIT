package pl.jakubkonkol.tasteitserver.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Document(collection = "users")
@Data
public class User implements UserDetails {
    @Id
    private String userId;
    private String email;
    private String displayName = "guest";
    private String bio = "";
    private String profilePicture = "deafult-pic-id"; //TODO podmienić potem id jak już będziemy mieli foto
    private LocalDate createdAt;
    private LocalDate birthDate = LocalDate.of(2500, 1, 1);;
    private Authentication authentication;
    private Boolean firstLogin = true;
    private List<String> roles = List.of("USER");
    @DBRef
    private List<Tag> mainTags = new ArrayList<Tag>();
    @DBRef
    private List<Tag> customTags = new ArrayList<Tag>();
    private List<String> followers = new ArrayList<>();
    private List<String> following = new ArrayList<>();
    private List<FoodList> foodLists = new ArrayList<FoodList>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(r -> (GrantedAuthority) () -> r).toList();
    }

    /**
     * Tutaj trzeba dodac brakujace pola
     */

    @Override
    public String getPassword() {
        return authentication.getPassword();
    }

    @Override
    public String getUsername() {
        return email;
    }
}
