package pl.jakubkonkol.tasteitserver.service.interfaces;

import pl.jakubkonkol.tasteitserver.dto.AuthenticationSuccessTokenDto;
import pl.jakubkonkol.tasteitserver.dto.UserCreationRequestDto;
import pl.jakubkonkol.tasteitserver.dto.UserLoginRequestDto;
import pl.jakubkonkol.tasteitserver.model.User;

public interface IAuthenticationService {
    User register(final UserCreationRequestDto userCreationRequestDto);
    AuthenticationSuccessTokenDto login(final UserLoginRequestDto userLoginRequestDto);
    void logout(final String sessionToken);
}
