package pl.jakubkonkol.tasteitserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserLoginRequestDto {
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Provided email has an invalid format")
    private String email;
    @NotBlank(message = "Field password cannot be blank and cannot be null")
    @Pattern(regexp =
//            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s])[A-Za-z\\d!\"#$%&'()*+,\\-./:;<=>?@[\\\\\\]^_`{|}~]{8,20}$"
            "^(?=.*[0-9])"
            + "(?=.*[a-z])(?=.*[A-Z])"
            + "(?=.*[!#$%&'()*+,\\-./:;<=>?@\\[\\\\\\]^_`{|}~])"
            + "(?=\\S+$).{6,20}$"
            , message = "Provided password has an invalid format. " +
            "Should be: 6-20 characters long, contains at least one digit, one lowercase letter, " +
            "one uppercase letter, one special character '  ', and has no whitespace")
    private String password;


}

