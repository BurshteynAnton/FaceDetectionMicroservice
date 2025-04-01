package antonBurshteyn.login.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "Response returned after authentication")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

    public AuthenticationResponse(String error, int status) {
        this.error = error;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @JsonProperty("access_token")
    private String accessToken;

    @Schema(description = "Error message if present", example = "Invalid credentials")
    private String error;

    @Schema(description = "HTTP status code", example = "401")
    private Integer status;

    @Schema(description = "Timestamp of error or response", example = "1700000000000")
    private Long timestamp;
}