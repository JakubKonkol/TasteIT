package pl.jakubkonkol.tasteitserver.e2e;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.jakubkonkol.tasteitserver.dto.*;
import pl.jakubkonkol.tasteitserver.model.Measurement;
import pl.jakubkonkol.tasteitserver.model.PostMedia;
import pl.jakubkonkol.tasteitserver.model.Recipe;
import pl.jakubkonkol.tasteitserver.model.enums.PostType;
import pl.jakubkonkol.tasteitserver.model.IngredientWrapper;

import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import pl.jakubkonkol.tasteitserver.service.*;
import pl.jakubkonkol.tasteitserver.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;


import org.bson.Document;

@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
@TestMethodOrder(OrderAnnotation.class)
public class UserFlowE2ETest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostValidationService postValidationService;

    private static String sessionToken;
    private static String userId;
    private static String postId;
    private static String adminSessionToken;

    private static final String TEST_PASSWORD = "Test123!@#";
    private static final String TEST_EMAIL = "test@example.com";

    //Check if mongo is up
    @BeforeAll
    static void init(@Autowired MongoTemplate mongoTemplate) {
        try {
            mongoTemplate.getDb().runCommand(new Document("ping", 1));
        } catch (Exception e) {
            throw new IllegalStateException("MongoDB is not available", e);
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should register user")
    void shouldRegisterUser() throws Exception {
        // Given
        UserCreationRequestDto requestDto = new UserCreationRequestDto();
        requestDto.setEmail(TEST_EMAIL);
        requestDto.setPassword(TEST_PASSWORD);

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // Use JsonNode to deserialize
        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        userId = jsonNode.get("userId").asText();
        assertThat(userId).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("Should login user")
    void shouldLoginUser() throws Exception {
        // Given
        UserLoginRequestDto requestDto = new UserLoginRequestDto();
        requestDto.setEmail(TEST_EMAIL);
        requestDto.setPassword(TEST_PASSWORD);

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        AuthenticationSuccessTokenDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthenticationSuccessTokenDto.class
        );
        sessionToken = response.getSessionToken();
        assertThat(sessionToken).isNotNull();
    }

    @Test
    @Order(3)
    @DisplayName("Should update user profile")
    void shouldUpdateUserProfile() throws Exception {
        assumeTrue(sessionToken != null, "Session token is required for this test");

        // Given
        UserProfileDto profileDto = new UserProfileDto();
        profileDto.setUserId(userId);
        profileDto.setDisplayName("Test User");
        profileDto.setBio("Test bio");
        profileDto.setProfilePicture("https://example.com/picture.jpg");
        Date specificDate = new Date(1735689600000L); // 01.01.2025
        profileDto.setBirthDate(specificDate);

        // When
        mockMvc.perform(put("/api/v1/user/update-user-profile")
                        .header("Authorization", sessionToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileDto)))
                .andExpect(status().isOk());

        // Then
        mockMvc.perform(get("/api/v1/user/" + userId)
                        .header("Authorization", sessionToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.displayName").value("Test User"))
                .andExpect(jsonPath("$.bio").value("Test bio"))
                .andExpect(jsonPath("$.profilePicture").value("https://example.com/picture.jpg"))
                .andExpect(jsonPath("$.birthDate").value("2025-01-01T00:00:00.000+00:00"));
    }

    @Test
    @Order(4)
    @DisplayName("Should get random feed")
    void shouldGetRandomFeed() throws Exception {
        assumeTrue(sessionToken != null, "Session token is required for this test");
        // When & Then
        mockMvc.perform(get("/api/v1/post/random-feed")
                        .header("Authorization", sessionToken)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    @Order(5)
    @DisplayName("Should get clastered feed")
    void shouldGetFeed() throws Exception {
        assumeTrue(sessionToken != null, "Session token is required for this test");

        // Request clustering
        mockMvc.perform(get("/api/v1/feed/request-clustering")
                        .header("Authorization", sessionToken))
                .andExpect(status().isOk());

        // Analyze user preferences
        mockMvc.perform(post("/api/v1/feed/analyze/" + userId)
                        .header("Authorization", sessionToken))
                .andExpect(status().isOk());

        // Get ranked feed
        var result = mockMvc.perform(get("/api/v1/feed/ranked-feed")
                        .header("Authorization", sessionToken)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andReturn();

//        If you want to show results, uncomment these lines
//        String responseContent = result.getResponse().getContentAsString();
//        System.out.println("\n=== Feed Content ===");
//        System.out.println(objectMapper.writerWithDefaultPrettyPrinter()
//                .writeValueAsString(objectMapper.readTree(responseContent)));
//        System.out.println("==================\n");
    }

    @Test
    @Order(6)
    @DisplayName("Should verify post interactions")
    void shouldVerifyPostInteractions() throws Exception {
        // Given
        PostDto postDto = new PostDto();
        PostMedia postMedia = new PostMedia();
        postMedia.setTitle("Test Post");
        postMedia.setPictures(List.of("test-picture-url"));
        postMedia.setDescription("Test Description");
        postDto.setPostMedia(postMedia);
        postDto.setPostType(PostType.FOOD);

        PostAuthorDto authorDto = new PostAuthorDto();
        authorDto.setUserId(userId);
        postDto.setPostAuthorDto(authorDto);

        // Adding Recipe
        Recipe recipe = new Recipe();
        Map<Integer, String> steps = new HashMap<>();
        steps.put(1, "First step");
        recipe.setSteps(steps);
        
        // Adding measurement to recipe
        List<IngredientWrapper> ingredients = new ArrayList<>();
        IngredientWrapper ingredient = new IngredientWrapper();
        ingredient.setName("Test Ingredient");
        Measurement measurement = new Measurement();
        measurement.setValue("100");
        measurement.setUnit("g");
        ingredient.setMeasurement(measurement);

        ingredients.add(ingredient);
        recipe.setIngredientsWithMeasurements(ingredients);
        
        postDto.setRecipe(recipe);

        // When - Create post
        MvcResult createResult = mockMvc.perform(post("/api/v1/post/create")
                        .header("Authorization", sessionToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andReturn();

        // Extract post ID from response
        String postId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("postId")
                .asText();

        // Then - Get post and verify
        mockMvc.perform(get("/api/v1/post/" + postId)
                        .header("Authorization", sessionToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(postId))
                .andExpect(jsonPath("$.postMedia.title").value("Test Post"))
                .andExpect(jsonPath("$.postType").value("FOOD"));

        // Cleanup
        mockMvc.perform(delete("/api/v1/post/" + postId)
                        .header("Authorization", sessionToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(7)
    @DisplayName("Should create post")
    void shouldCreatePost() throws Exception {
        // Show avaible types
        System.out.println("Dostępne typy postów: " + Arrays.toString(PostType.values()));

        // Given
        PostDto postDto = new PostDto();
        PostMedia postMedia = new PostMedia();
        postMedia.setTitle("Test post");
        postMedia.setPictures(List.of("https://example.com/image.jpg"));
        postMedia.setDescription("Test description");
        postDto.setPostMedia(postMedia);
        postDto.setPostType(PostType.FOOD);

        PostAuthorDto authorDto = new PostAuthorDto();
        authorDto.setUserId(userId);
        postDto.setPostAuthorDto(authorDto);

        // Adding Recipe
        Recipe recipe = new Recipe();
        Map<Integer, String> steps = new HashMap<>();
        steps.put(1, "First step");
        recipe.setSteps(steps);

        // Adding measurement to recipe
        List<IngredientWrapper> ingredients = new ArrayList<>();
        IngredientWrapper ingredient = new IngredientWrapper();
        ingredient.setName("Test Ingredient");
        Measurement measurement = new Measurement();
        measurement.setValue("100");
        measurement.setUnit("g");
        ingredient.setMeasurement(measurement);

        ingredients.add(ingredient);
        recipe.setIngredientsWithMeasurements(ingredients);

        postDto.setRecipe(recipe);

        // When
        MvcResult result = mockMvc.perform(post("/api/v1/post/create")
                        .header("Authorization", sessionToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andReturn();

        // Then

        PostDto createdPost = objectMapper.readValue(result.getResponse().getContentAsString(),
                PostDto.class);
        postId = createdPost.getPostId();
        assertThat(postId).isNotNull();
    }

    @Test
    @Order(8)
    @DisplayName("Should like post")
    void shouldLikePost() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/post/" + postId + "/like")
                        .header("Authorization", sessionToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(9)
    @DisplayName("Should add comment to post")
    void shouldAddComment() throws Exception {
        // Given
        CommentDto commentDto = new CommentDto();
        commentDto.setContent("Test comment");

        // When & Then
        mockMvc.perform(post("/api/v1/post/" + postId + "/comment")
                        .header("Authorization", sessionToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Test comment"));
    }

    @BeforeEach
    void setUp(TestInfo testInfo) throws Exception {
        assumeTrue(sessionToken != null ||
                        testInfo.getTestMethod().get().getName().equals("shouldRegisterUser") ||
                        testInfo.getTestMethod().get().getName().equals("shouldLoginUser"),
                "Session token is required for this test");

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());

        // Clear test user before registration test
        if (testInfo.getTestMethod().get().getName().equals("shouldRegisterUser")) {
            cleanupTestUser();
        }

        System.setProperty("ADMIN_PASSWORD", "admin123!@#");
    }

    private void cleanupTestUser() {
        String adminSessionToken = null;
        try {
            // Login as admin
            UserLoginRequestDto adminLogin = new UserLoginRequestDto();
            adminLogin.setEmail("tasteit@admin.com");
            adminLogin.setPassword(System.getenv("MONGO_PASSWORD"));

            MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(adminLogin)))
                    .andReturn();

            if (loginResult.getResponse().getStatus() == 200) {
                JsonNode loginResponse = objectMapper.readTree(
                        loginResult.getResponse().getContentAsString());
                adminSessionToken = loginResponse.get("sessionToken").asText();
            }

            if (postId != null) {
                // Delete post comments
                List<CommentDto> comments = commentService.getComments(postId);
                comments.forEach(comment -> {
                    try {
                        commentService.deleteComment(postId, comment.getCommentId(), sessionToken);
                    } catch (Exception e) {
                        System.err.println("Error deleting comment: " + e.getMessage());
                    }
                });

                // Delete post likes
                try {
                    likeService.unlikePost(postId, sessionToken);
                } catch (Exception e) {
                    System.err.println("Error unliking post: " + e.getMessage());
                }

                // Delete test post
                try {
                    postValidationService.deletePost(postId, sessionToken);
                } catch (Exception e) {
                    System.err.println("Error deleting post: " + e.getMessage());
                }
            }

            // Delete test user
            try {
                if (adminSessionToken != null) {
                    userService.deleteUser(TEST_EMAIL, adminSessionToken);
                } else {
                    userRepository.deleteByEmail(TEST_EMAIL);
                }
            } catch (Exception e) {
                System.err.println("Error deleting user: " + e.getMessage());
                try {
                    userRepository.deleteByEmail(TEST_EMAIL);
                } catch (Exception ex) {
                    System.err.println("Error during user cleanup: " + ex.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    @AfterAll
    static void cleanup(@Autowired CommentService commentService,
                        @Autowired LikeService likeService,
                        @Autowired MockMvc mockMvc,
                        @Autowired ObjectMapper objectMapper,
                        @Autowired UserRepository userRepository,
                        @Autowired UserService userService,
                        @Autowired PostValidationService postValidationService
    ) {
        try {
            if (postId != null) {
                // Delete post comments
                List<CommentDto> comments = commentService.getComments(postId);
                comments.forEach(comment -> {
                    try {
                        commentService.deleteComment(postId, comment.getCommentId(), sessionToken);
                    } catch (Exception e) {
                        System.err.println("Error deleting comment: " + e.getMessage());
                    }
                });

                // Delete post likes
                try {
                    likeService.unlikePost(postId, sessionToken);
                } catch (Exception e) {
                    System.err.println("Error unliking post: " + e.getMessage());
                }

                // Delete test post
                try {
                    postValidationService.deletePost(postId, sessionToken);
                } catch (Exception e) {
                    System.err.println("Error deleting post: " + e.getMessage());
                }
            }

            // Delete test user
            if (userId != null) {
                try {
                    if (adminSessionToken != null) {
                        userService.deleteUser(TEST_EMAIL, adminSessionToken);
                    } else {
                        userRepository.deleteByEmail(TEST_EMAIL);
                    }
                } catch (Exception e) {
                    System.err.println("Error deleting user: " + e.getMessage());
                    try {
                        userRepository.deleteByEmail(TEST_EMAIL);
                    } catch (Exception ex) {
                        System.err.println("Error during user cleanup: " + ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }
} 