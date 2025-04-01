package antonBurshteyn.login.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "Request object for user login")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {

    @Schema(example = "anton.burshteyn@example.com")
    private String email;

    @Schema(example = "StrongPass123!")
    private String password;
}