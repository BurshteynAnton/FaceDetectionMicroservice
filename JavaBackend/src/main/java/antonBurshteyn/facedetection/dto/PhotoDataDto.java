package antonBurshteyn.facedetection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "DTO representing photo data and validation status")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhotoDataDto {

    @Schema(description = "Photo binary data encoded as base64 string", example = "iVBORw0KGgoAAAANSUhEUgAA...")
    private String data;

    @Schema(description = "Name of the photo", example = "photo_123.jpg")
    private String name;

    @Schema(description = "Status of photo validation", example = "SUCCESS")
    private String status;
}