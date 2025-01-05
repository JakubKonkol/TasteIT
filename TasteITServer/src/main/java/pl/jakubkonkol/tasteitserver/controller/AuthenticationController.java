package pl.jakubkonkol.tasteitserver.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jakubkonkol.tasteitserver.dto.AuthenticationSuccessTokenDto;
import pl.jakubkonkol.tasteitserver.dto.UserCreationRequestDto;
import pl.jakubkonkol.tasteitserver.dto.UserLoginRequestDto;
import pl.jakubkonkol.tasteitserver.model.GenericResponse;
import pl.jakubkonkol.tasteitserver.model.User;
import pl.jakubkonkol.tasteitserver.service.interfaces.IAuthenticationService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final IAuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<User> createUser(@Valid @RequestBody final UserCreationRequestDto userCreationRequest) {
        return ResponseEntity.ok(authenticationService.register(userCreationRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationSuccessTokenDto> login(@Valid @RequestBody final UserLoginRequestDto userLoginRequest) {
        return ResponseEntity.ok(authenticationService.login(userLoginRequest));
    }
    @GetMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") final String sessionToken) {
        authenticationService.logout(sessionToken);
        return ResponseEntity.ok().body(GenericResponse
                .builder()
                .status(HttpStatus.OK.value()).
                message("Logged out")
                .build());
    }
}
