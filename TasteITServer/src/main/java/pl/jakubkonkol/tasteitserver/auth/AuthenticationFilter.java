package pl.jakubkonkol.tasteitserver.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.jakubkonkol.tasteitserver.model.ErrorResponse;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.repository.UserRepository;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest req, @NotNull HttpServletResponse res, @NotNull FilterChain filterChain) throws ServletException, IOException {
        String sessionToken = req.getHeader("Authorization");
        if(sessionToken == null || sessionToken.isEmpty()){
            filterChain.doFilter(req, res);
            return;
        }
        Optional<User> optionalUser = userRepository.findBySessionToken(sessionToken);

        if (optionalUser.isEmpty()) {
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            ObjectMapper mapper = new ObjectMapper();
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpServletResponse.SC_UNAUTHORIZED)
                    .message("Unauthorized")
                    .build();
            mapper.writeValue(res.getWriter(), errorResponse);
            return;
        }
        User user = optionalUser.get();
        req.setAttribute("identity", user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        authentication.setDetails(user.getUserId());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(req, res);
    }
}
