package pl.jakubkonkol.tasteitserver.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CryptoTools {
    private static final String SECRET_PHRASE_NAME = "TASTEIT_SECRET_PHRASE";
    private static final String SECRET = System.getenv(SECRET_PHRASE_NAME);

    public String generateSalt(){
        return BCrypt.gensalt();
    }
    public String authentication(String password, String salt){
        return BCrypt.hashpw(password + SECRET, salt);
    }
}
