package productdetector.payload;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SigninRequestDto {

    @NotBlank
    private String usernameOrEmail;

    @NotBlank
    private String password;

}
