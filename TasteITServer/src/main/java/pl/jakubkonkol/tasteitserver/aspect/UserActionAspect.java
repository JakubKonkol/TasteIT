package pl.jakubkonkol.tasteitserver.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.aspectj.lang.annotation.Aspect;
import pl.jakubkonkol.tasteitserver.annotation.RegisterAction;
import pl.jakubkonkol.tasteitserver.dto.CommentDto;
import pl.jakubkonkol.tasteitserver.dto.PostDto;
import pl.jakubkonkol.tasteitserver.model.UserAction;
import pl.jakubkonkol.tasteitserver.service.UserService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Aspect
@Component
@RequiredArgsConstructor
public class UserActionAspect {
    private final MongoTemplate mongoTemplate;
    private final UserService userService;

    @AfterReturning("@annotation(registerAction)")
    public void logAction(JoinPoint joinPoint, RegisterAction registerAction) {
        try {
            String userId = userService.getCurrentUserId();
            UserAction action = new UserAction();
            action.setActionType(registerAction.actionType());
            action.setUserId(userId);
            action.setTimestamp(new Date());

            // Extrakcja metadanych na podstawie typu akcji
            Map<String, Object> metadata = extractMetadata(joinPoint, registerAction.actionType());
            action.setMetadata(metadata);

            // Asynchroniczny zapis do bazy
            CompletableFuture.runAsync(() -> {
                mongoTemplate.save(action, "userActions");
            });
        } catch (Exception e) {
            // Logowanie błędu, ale nie przerywanie głównego procesu
            System.err.println("Error logging user action: " + e.getMessage());
        }
    }

    private Map<String, Object> extractMetadata(JoinPoint joinPoint, String actionType) {
        Map<String, Object> metadata = new HashMap<>();
        Object[] args = joinPoint.getArgs();

        switch (actionType) {
            case "LIKE_POST":
                if (args.length > 0) {
                    metadata.put("postId", args[0]);
                }
                break;
            case "COMMENT_POST":
                if (args.length > 1) {
                    metadata.put("postId", args[0]);
                    metadata.put("commentContent", ((CommentDto)args[1]).getContent());
                }
                break;
            case "ADD_TO_FOODLIST":
                if (args.length > 2) {
                    metadata.put("foodListId", args[1]);
                    metadata.put("postId", ((PostDto)args[2]).getPostId());
                }
                break;
        }

        return metadata;
    }
}