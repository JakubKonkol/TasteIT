package pl.jakubkonkol.tasteitserver.config;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.repository.UserRepository;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final UserRepository userRepository;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // This configures the message routing paths:
        // /topic is used for broadcasts (like global notifications)
        // /user is for private messages (like personal notifications)
        config.enableSimpleBroker("/topic", "/user");

        // When clients send messages to the server, they'll start with /app
        config.setApplicationDestinationPrefixes("/app");

        // This prefix is used for user-specific subscriptions
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // This creates the WebSocket endpoint that clients will connect to
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000")  // frontend URL
                .withSockJS();  // Provides fallback options for older browsers
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                        message, StompHeaderAccessor.class);

                // We only want to authenticate during the initial CONNECT
                assert accessor != null;
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Get the session token from headers
                    String sessionToken = accessor.getFirstNativeHeader("Authorization");

                    if (sessionToken != null) {
                        // Try to find the user with this session token
                        Optional<User> userOptional = userRepository.findBySessionToken(sessionToken);

                        if (userOptional.isPresent()) {
                            User user = userOptional.get();
                            // Create a Principal object that Spring will use to identify the user
                            accessor.setUser(new Principal() {
                                @Override
                                public String getName() {
                                    return user.getUserId();
                                }
                            });

                            // You can also set user attributes that might be useful later
                            accessor.setSessionAttributes(Map.of(
                                    "userId", user.getUserId(),
                                    "email", user.getEmail()
                            ));
                        }
                    }
                }
                return message;
            }
        });
    }
}