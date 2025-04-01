package antonBurshteyn.login.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @Schema(example = "Anton")
    private String firstName;
    @Schema(example = "Burshteyn")
    private String lastName;
    @Schema(example = "anton.burshteyn@example.com")
    private String email;
    @Schema(example = "StrongPass123!")
    private String password;
}
