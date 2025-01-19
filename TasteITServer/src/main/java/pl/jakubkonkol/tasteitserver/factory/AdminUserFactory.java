package pl.jakubkonkol.tasteitserver.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jakubkonkol.tasteitserver.dto.UserCreationRequestDto;
import pl.jakubkonkol.tasteitserver.dto.UserProfileDto;
import pl.jakubkonkol.tasteitserver.exception.ResourceNotFoundException;
import pl.jakubkonkol.tasteitserver.repository.UserRepository;
import pl.jakubkonkol.tasteitserver.service.interfaces.IAuthenticationService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IUserService;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class AdminUserFactory {
    private final UserRepository userRepository;
    private final IAuthenticationService authenticationService;
    private final IUserService userService;
    private static final Logger LOGGER = Logger.getLogger(AdminUserFactory.class.getName());
    private final List<String> roles = List.of("ADMIN", "USER");
    private static final String ADMIN_EMAIL = "tasteit@admin.com";
    private static final String ADMIN_NAME = "TasteIT";
    private static final String ADMIN_BIO = "Admin of TasteIT";
    private static final String ADMIN_PICTURE = "https://github.com/JakubKonkol/TasteIT/blob/master/assets/icon.png?raw=true";

    public void CreateAdmin() {
        if (userRepository.findByEmail(ADMIN_EMAIL).isEmpty()){

            //register admin account
            UserCreationRequestDto adminCreationDto = new UserCreationRequestDto();
            adminCreationDto.setEmail(ADMIN_EMAIL);
            adminCreationDto.setPassword(System.getenv("MONGO_PASSWORD"));
            authenticationService.register(adminCreationDto);

            // Force admin account to have userId 0
            var admin = userRepository.findByEmail(ADMIN_EMAIL).orElseThrow(() -> new ResourceNotFoundException(
                    "Unexpected error occurred while creating admin account"));

            var idToRemove = admin.getUserId();
            userRepository.deleteById(idToRemove);
            admin.setUserId("0");
            userRepository.save(admin);

            //set admin profile
            UserProfileDto adminProfile = new UserProfileDto();
            adminProfile.setUserId("0");
            adminProfile.setDisplayName(ADMIN_NAME);
            adminProfile.setBio(ADMIN_BIO);
            adminProfile.setProfilePicture(ADMIN_PICTURE);
            adminProfile.setBirthDate(new Date());
            userService.updateUserProfile(adminProfile, null);
            userRepository.findById("0").ifPresent(user -> {
                user.setRoles(roles);
                userRepository.save(user);
            });
            LOGGER.log(Level.INFO, "Admin account created");
        } else{
            LOGGER.log(Level.INFO, "Admin account already exist");
        }
    }
}
