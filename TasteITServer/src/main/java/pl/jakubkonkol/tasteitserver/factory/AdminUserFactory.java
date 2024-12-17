package pl.jakubkonkol.tasteitserver.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jakubkonkol.tasteitserver.dto.UserCreationRequestDto;
import pl.jakubkonkol.tasteitserver.dto.UserProfileDto;
import pl.jakubkonkol.tasteitserver.repository.UserRepository;
import pl.jakubkonkol.tasteitserver.service.interfaces.IAuthenticationService;
import pl.jakubkonkol.tasteitserver.service.interfaces.IUserService;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
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
    public void CreateAdmin() {
        if (userRepository.findByEmail("tasteit@admin.com").isEmpty()){

            //register admin account
            UserCreationRequestDto adminCreationDto = new UserCreationRequestDto();
            adminCreationDto.setEmail("tasteit@admin.com");
            adminCreationDto.setPassword(System.getenv("MONGO_PASSWORD"));
            authenticationService.register(adminCreationDto);

            // Force admin account to have userId 0 (Voodoo magic)
            var admin = userRepository.findByEmail("tasteit@admin.com").orElseThrow(() -> new NoSuchElementException(
                    "Unexpected error occurred while creating admin account"));
            var idToRemove = admin.getUserId();
            userRepository.deleteById(idToRemove);
            admin.setUserId("0");
            userRepository.save(admin);

            //set admin profile
            UserProfileDto adminProfile = new UserProfileDto();
            adminProfile.setUserId("0");
            adminProfile.setDisplayName("TasteIT");
            adminProfile.setBio("Admin of TasteIT");
            adminProfile.setProfilePicture("https://github.com/JakubKonkol/TasteIT/blob/master/assets/icon.png?raw=true");
            adminProfile.setBirthDate(new Date());
            userService.updateUserProfile(adminProfile, null);
//            userService.changeUserFirstLogin("0");
            userRepository.findById("0").ifPresent(user -> {
                user.setRoles(roles);
                userRepository.save(user);
            });
            LOGGER.log(Level.INFO, "Admin account created");
        }else{
            LOGGER.log(Level.INFO, "Admin account already exist");
        }
    }
}
