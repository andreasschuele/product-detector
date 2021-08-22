package productdetector.payload;

import lombok.*;

@Data
public class JwtAuthenticationResponseDto {

    private String accessToken;

    private String tokenType = "Bearer";

    private String role;

}
