package pl.jakubkonkol.tasteitserver.dto;

import lombok.Data;

@Data
public class PostAuthorDto {
    private String userId;
    private String displayName;
    private String profilePicture;
}
