package antonBurshteyn.dto;

import antonBurshteyn.enums.PhotoValidationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "Response returned after photo validation")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhotoValidationResponseDto {

    @Schema(description = "Name of the validated photo", example = "user1_photo.jpg")
    private String name;

    @Schema(description = "Validation status", example = "SUCCESS")
    private PhotoValidationStatus status;

    @Schema(description = "Additional message or error detail", example = "Photo validated and saved successfully")
    private String message;
}