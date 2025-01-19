package pl.jakubkonkol.tasteitserver.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Authentication {
    private String password;
    private String salt;
    private String sessionToken;
}
